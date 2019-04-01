package com.catchingnow.delegatedscopeclient;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author heruoxin @ CatchingNow Inc.
 * @since 2019-04-01
 */
class DSMClinetImplement extends DSMClient {
    private static final String METHOD_GET_SELF_SCOPES = "CenterAppBridge:METHOD_GET_SELF_SCOPES";
    private static final String METHOD_GET_SDK_VERSION = "CenterAppBridge:METHOD_GET_SDK_VERSION";

    private static final String METHOD_INSTALL_APP = "CenterAppBridge:METHOD_INSTALL_APP";
    private static final String METHOD_UNINSTALL_APP = "CenterAppBridge:METHOD_UNINSTALL_APP";
    private static final String METHOD_SET_APP_OPS = "CenterAppBridge:METHOD_SET_APP_OPS";

    private static final String EXTRA_SCOPES = "CenterAppBridge:EXTRA_SCOPES";
    private static final String EXTRA_SDK_VERSION = "CenterAppBridge:EXTRA_SDK_VERSION";
    private static final String EXTRA_APP_OP_CODE = "CenterAppBridge:EXTRA_APP_OP_CODE";
    private static final String EXTRA_APP_OP_MODE = "CenterAppBridge:EXTRA_APP_OP_MODE";

    public static String getOwnerPackageName(Context context) {
        int flag = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            flag = PackageManager.MATCH_DISABLED_COMPONENTS;
        }
        DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (manager == null) return null;
        for (ResolveInfo ri : context.getPackageManager().queryIntentActivities(new Intent(ACTION_LIST), flag)) {
            String packageName = ri.activityInfo.packageName;
            if (manager.isDeviceOwnerApp(packageName)) return packageName;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (manager.isProfileOwnerApp(packageName)) return packageName;
            }
        }
        return null;
    }

    private static String sDeviceOwner = null;

    private static String getOwnerPackageNameInternal(Context context) {
        if (TextUtils.isEmpty(sDeviceOwner)) {
             sDeviceOwner = getOwnerPackageName(context);
        }
        if (TextUtils.isEmpty(sDeviceOwner)) {
            throw new NullPointerException("Unknown ownerPackageName!");
        }
        return sDeviceOwner;
    }

    public static int getOwnerSDKVersion(Context context) {
        try {
            Bundle call = context.getContentResolver().call(Uri.parse("content://" + getOwnerPackageNameInternal(context) +
                            ".DSM_CENTER"),
                    METHOD_GET_SDK_VERSION, null, new Bundle());
            return call.getInt(EXTRA_SDK_VERSION, -1);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @NonNull
    public static List<String> getDelegatedScopes(Context context) {
        try {
            Bundle call = context.getContentResolver().call(Uri.parse("content://" + getOwnerPackageNameInternal(context) +
                            ".DSM_CENTER"),
                    METHOD_GET_SELF_SCOPES, null, new Bundle());
            return call.getStringArrayList(EXTRA_SCOPES);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void installApp(Context context, Uri apkUri, @Nullable String packageName) throws Exception {
        String ownerPackageName = getOwnerPackageNameInternal(context);
        context.grantUriPermission(ownerPackageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            Bundle bundle = new Bundle();
            bundle.putParcelable(Intent.EXTRA_ORIGINATING_URI, apkUri);
            bundle.putString(Intent.EXTRA_PACKAGE_NAME, packageName);
            Bundle call = context.getContentResolver().call(Uri.parse("content://" + ownerPackageName +
                            ".DSM_CENTER"),
                    METHOD_INSTALL_APP, null, bundle);
            if (call != null && call.containsKey(Intent.ACTION_APP_ERROR)) {
                throw (Exception) call.getSerializable(Intent.ACTION_APP_ERROR);
            }
        } finally {
            context.revokeUriPermission(ownerPackageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void uninstallApp(Context context, String packageName) throws Exception {
        Bundle bundle = new Bundle();
        bundle.putString(Intent.EXTRA_PACKAGE_NAME, packageName);
        Bundle call = context.getContentResolver().call(Uri.parse("content://" + getOwnerPackageNameInternal(context) +
                        ".DSM_CENTER"),
                METHOD_UNINSTALL_APP, null, bundle);
        if (call != null && call.containsKey(Intent.ACTION_APP_ERROR)) {
            throw (Exception) call.getSerializable(Intent.ACTION_APP_ERROR);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public static void setAppOpsMode(Context context, int opCode, int uid, String packageName, int mode) throws Exception {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_APP_OP_CODE, opCode);
        bundle.putInt(Intent.EXTRA_UID, uid);
        bundle.putString(Intent.EXTRA_PACKAGE_NAME, packageName);
        bundle.putInt(EXTRA_APP_OP_MODE, mode);
        Bundle call = context.getContentResolver().call(Uri.parse("content://" + getOwnerPackageNameInternal(context) +
                        ".DSM_CENTER"),
                METHOD_SET_APP_OPS, null, bundle);
        if (call != null && call.containsKey(Intent.ACTION_APP_ERROR)) {
            throw (Exception) call.getSerializable(Intent.ACTION_APP_ERROR);
        }
    }

}
