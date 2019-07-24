package com.catchingnow.delegatedscopesmanager.centerApp;

import android.annotation.SuppressLint;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.ArrayMap;

import com.catchingnow.delegatedscopesmanager.ui.AppListActivity;
import com.catchingnow.delegatedscopesmanager.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by web1n.
 */
class CenterAppInternal implements CenterApp {

    private static final String DEVICE_ADMIN_ACTION = "android.app.develop.action.DEVICE_DELEGATION";
    private static final String DEVICE_ADMIN_META_DATA = "android.app.develop.delegation";
    private static final String ROOT_TAG = "device-delegation";
    private static final String START_TAG = "uses-policies";

    private static final Map<String, Integer> sKnownPolicies;
    private static final Map<String, Integer> sKnownCustomPolicies;

    static  {
        sKnownPolicies = new ArrayMap<>();
        sKnownPolicies.put("delegation-app-restrictions", R.string.delegation_app_restrictions);
        sKnownPolicies.put("delegation-block-uninstall", R.string.delegation_block_uninstall);
        sKnownPolicies.put("delegation-cert-install", R.string.delegation_cert_install);
        sKnownPolicies.put("delegation-enable-system-app", R.string.delegation_enable_system_app);
        sKnownPolicies.put("delegation-install-existing-package", R.string.delegation_install_existing_package);
        sKnownPolicies.put("delegation-keep-uninstalled-packages", R.string.delegation_keep_uninstalled_packages);
        sKnownPolicies.put("delegation-package-access", R.string.delegation_package_access);
        sKnownPolicies.put("delegation-permission-grant", R.string.delegation_permission_grant);

        sKnownCustomPolicies = new ArrayMap<>();
        if (Build.VERSION.SDK_INT >= 23) {
            sKnownCustomPolicies.put("dsm-delegation-install-uninstall-app", R.string.dsm_delegation_install_uninstall_app);
        }
        if (Build.VERSION.SDK_INT >= 28) {
            sKnownCustomPolicies.put("dsm-delegation-set-app-ops", R.string.dsm_delegation_set_app_ops);
        }
    }

    private final PackageManager mPackageManager;
    private final DevicePolicyManager mDevicePolicyManager;
    private final Context mContext;
    private final SharedPreferences mPreferences;
    @Nullable
    private ComponentName mAdmin;

    private final HashMap<String,  List<String>> mDelegatedScopesCache = new HashMap<>();

    @SuppressLint("StaticFieldLeak")
    private static CenterAppInternal sInstance;

    static CenterAppInternal getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CenterAppInternal(context);
        }
        return sInstance;
    }

    private CenterAppInternal(Context context) {
        this.mPackageManager = context.getPackageManager();
        this.mDevicePolicyManager = ContextCompat.getSystemService(context, DevicePolicyManager.class);
        this.mPreferences = context.getSharedPreferences("dsm-device-delegations", Context.MODE_PRIVATE);
        this.mContext = context.getApplicationContext();
    }

    @NonNull
    private static ArrayList<String> getSystemKnownPolicies() {
        ArrayList<String> systemKnownPolicies = new ArrayList<>();
        for (Field field : DevicePolicyManager.class.getFields()) {
            if (!field.getName().startsWith("DELEGATION_")) continue;
            if (!field.getType().getName().equals(java.lang.String.class.getName())) continue;
            String policy = field.getName().toLowerCase().replaceAll("_", "-");
            systemKnownPolicies.add(policy);
        }
        return systemKnownPolicies;
    }

    @Override
    @NonNull
    public List<ApplicationInfo> getDelegationApps() {
        List<ApplicationInfo> delegationApp = new ArrayList<>();

        Intent intent = new Intent(DEVICE_ADMIN_ACTION);
        for (ResolveInfo resolveInfo : mPackageManager.queryBroadcastReceivers(intent, 0)) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (packageName.equals(mContext.getPackageName())) continue;

            try {
                ApplicationInfo ai = mPackageManager.getApplicationInfo(packageName, 0);
                delegationApp.add(ai);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return delegationApp;
    }

    private List<String> getRequiredPoliciesForApp(ActivityInfo activityInfo) throws XmlPullParserException, IOException {
        ArrayList<String> systemKnownPolicies = getSystemKnownPolicies();
        ArrayList<String> usesPolicies = new ArrayList<>();

        XmlResourceParser parser = activityInfo.loadXmlMetaData(mPackageManager, DEVICE_ADMIN_META_DATA);
        if (parser == null) {
            throw new XmlPullParserException("No " + DEVICE_ADMIN_META_DATA + " meta-data");
        }

        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && type != XmlPullParser.START_TAG) {
        }
        String nodeName = parser.getName();
        if (!ROOT_TAG.equals(nodeName)) {
            throw new XmlPullParserException(
                    "Meta-data does not start with device-admin tag");
        }

        int outerDepth = parser.getDepth();
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                continue;
            }
            String tagName = parser.getName();
            if (tagName.equals(START_TAG)) {
                int innerDepth = parser.getDepth();
                while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                        && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                    if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) continue;
                    if (sKnownCustomPolicies.containsKey(parser.getName())
                            || systemKnownPolicies.contains(parser.getName()))
                        usesPolicies.add(parser.getName());
                }
            }

        }
        parser.close();

        return usesPolicies;
    }

    @Override
    @NonNull
    public List<String> getRequireScopes(String packageName) throws IOException, XmlPullParserException {
        Intent intent = new Intent(DEVICE_ADMIN_ACTION).setPackage(packageName);
        List<ResolveInfo> resolveInfos = mPackageManager.queryBroadcastReceivers(intent, PackageManager.GET_META_DATA);
        if (resolveInfos.size() > 0) {
            return getRequiredPoliciesForApp(resolveInfos.get(0).activityInfo);
        }
        throw new XmlPullParserException("can not query politics");
    }

    @NonNull
    @Override
    public List<String> getDelegatedScopes(String packageName) {
        if (mAdmin == null) refreshState();
        if (mAdmin == null) throw new IllegalStateException("Does not have device owner permission!");
        if (mDelegatedScopesCache.containsKey(packageName)) {
            return mDelegatedScopesCache.get(packageName);
        } else {
            List<String> scopes = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                scopes.addAll(mDevicePolicyManager.getDelegatedScopes(mAdmin, packageName));
            }
            scopes.addAll(mPreferences.getStringSet(packageName, new HashSet<>()));
            mDelegatedScopesCache.put(packageName, scopes);
            return scopes;
        }
    }

    @Override
    public void setDelegatedScopes(String packageName, List<String> scopes) {
        if (mAdmin == null) refreshState();
        if (mAdmin == null) throw new IllegalStateException("Does not have device owner permission!");
        List<String> systemScopes = new ArrayList<>();
        Set<String> customScopes = new HashSet<>();
        for (String scope : scopes) {
            if (sKnownCustomPolicies.containsKey(scope)) customScopes.add(scope);
            else if (sKnownPolicies.containsKey(scope)) systemScopes.add(scope);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mDevicePolicyManager.setDelegatedScopes(mAdmin, packageName, systemScopes);
        }
        mPreferences.edit()
                .putStringSet(packageName, customScopes)
                .apply();
        mDelegatedScopesCache.put(packageName, scopes);
    }

    @Override
    public void refreshState() {
        boolean isDeviceOwnerOrProfileOwner = mDevicePolicyManager.isDeviceOwnerApp(mContext.getPackageName())
                || mDevicePolicyManager.isProfileOwnerApp(mContext.getPackageName());
        if (isDeviceOwnerOrProfileOwner) {
            Intent intent = new Intent(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED)
                    .setPackage(mContext.getPackageName());
            List<ResolveInfo> resolveInfos = mPackageManager.queryBroadcastReceivers(intent, 0);
            if (resolveInfos != null && !resolveInfos.isEmpty()) {
                ResolveInfo ri = resolveInfos.get(0);
                mAdmin = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
            }
        } else {
            mAdmin = null;
        }
        isDeviceOwnerOrProfileOwner = isDeviceOwnerOrProfileOwner && mAdmin != null;
        ComponentName[] componentNames = new ComponentName[] {
                new ComponentName(mContext, AppListActivity.class),
                // todo add more components here
        };
        // 暂不自动禁用组件
    //    for (ComponentName c : componentNames) {
    //        mPackageManager.setComponentEnabledSetting(c,
    //                isDeviceOwnerOrProfileOwner? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
    //                PackageManager.DONT_KILL_APP);
    //    }
    }

    @Nullable
    @Override
    public String getScopeName(Context context, String scope) {
        if (sKnownCustomPolicies.containsKey(scope)) {
            //noinspection ConstantConditions
            return context.getString(sKnownCustomPolicies.get(scope));
        } else if (sKnownPolicies.containsKey(scope)) {
            //noinspection ConstantConditions
            return context.getString(sKnownPolicies.get(scope));
        } else {
            return null;
        }
    }

}