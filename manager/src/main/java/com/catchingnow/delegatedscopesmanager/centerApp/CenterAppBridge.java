package com.catchingnow.delegatedscopesmanager.centerApp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.catchingnow.delegatedscopesmanager.customAbilty.AppOpsUtil;
import com.catchingnow.delegatedscopesmanager.customAbilty.PackageInstallerUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author heruoxin @ CatchingNow Inc.
 * @since 2019-03-31
 */
public class CenterAppBridge {
    private static final String METHOD_GET_SELF_SCOPES = "CenterAppBridge:METHOD_GET_SELF_SCOPES";
    private static final String METHOD_GET_SDK_VERSION = "CenterAppBridge:METHOD_GET_SDK_VERSION";

    private static final String METHOD_INSTALL_APP = "CenterAppBridge:METHOD_INSTALL_APP";
    private static final String METHOD_UNINSTALL_APP = "CenterAppBridge:METHOD_UNINSTALL_APP";
    private static final String METHOD_SET_APP_OPS = "CenterAppBridge:METHOD_SET_APP_OPS";

    private static final String EXTRA_SCOPES = "CenterAppBridge:EXTRA_SCOPES";
    private static final String EXTRA_SDK_VERSION = "CenterAppBridge:EXTRA_SDK_VERSION";
    private static final String EXTRA_APP_OP_CODE = "CenterAppBridge:EXTRA_APP_OP_CODE";
    private static final String EXTRA_APP_OP_MODE = "CenterAppBridge:EXTRA_APP_OP_MODE";

    public static Bundle call(Context context, String callingPackage, @NonNull String method,
                              @Nullable String arg,
                              @Nullable Bundle extras) {
        if (extras == null) return null;
        switch (method) {
            case METHOD_GET_SDK_VERSION:
                return getSDK(context);
            case METHOD_GET_SELF_SCOPES:
                return getSelfScopes(context, callingPackage);
            case METHOD_INSTALL_APP:
                return permissionWrap("dsm-delegation-install-uninstall-app", context, callingPackage, extras,
                        CenterAppBridge::installApp);
            case METHOD_UNINSTALL_APP:
                return permissionWrap("dsm-delegation-install-uninstall-app", context, callingPackage, extras,
                        CenterAppBridge::uninstallApp);
            case METHOD_SET_APP_OPS:
                return permissionWrap("dsm-delegation-set-app-ops", context, callingPackage, extras,
                        CenterAppBridge::setAppOps);
        }
        return null;
    }

    private static Bundle getSDK(Context context) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_SDK_VERSION, CenterApp.SDK_VERSION);
        return bundle;
    }

    private static Bundle getSelfScopes(Context context, String callingPackage) {
        Bundle bundle = new Bundle();
        List<String> scopes = CenterApp.getInstance(context).getDelegatedScopes(callingPackage);
        bundle.putStringArrayList(EXTRA_SCOPES, new ArrayList<>(scopes));
        return bundle;
    }

    private static Bundle permissionWrap(String permission, Context context, String callingPackage, Bundle extras,
                                         Func func) {
        try {
            if (TextUtils.isEmpty(callingPackage)) throw new IllegalStateException("Unknown calling package!");
            if (!CenterApp.getInstance(context).getDelegatedScopes(callingPackage).contains(permission)) {
                throw new SecurityException("Package " + callingPackage + " dose not have permission " + permission + " !");
            }
            return func.apply(context, extras);
        } catch (Exception e) {
            return toError(e);
        }
    }

    @SuppressLint("NewApi")
    private static Bundle installApp(Context context, Bundle extras) throws IOException {
        PackageInstallerUtil.installPackage(context,
                extras.getParcelable(Intent.EXTRA_ORIGINATING_URI),
                extras.getString(Intent.EXTRA_PACKAGE_NAME));
        return new Bundle();
    }

    @SuppressLint("NewApi")
    private static Bundle uninstallApp(Context context, Bundle extras) {
        PackageInstallerUtil.uninstallPackage(context,
                extras.getString("android.intent.extra.PACKAGE_NAME"));
        return new Bundle();
    }

    @SuppressLint("NewApi")
    private static Bundle setAppOps(Context context, Bundle extras) {
        AppOpsUtil.setMode(context,
                extras.getInt(EXTRA_APP_OP_CODE),
                extras.getInt(Intent.EXTRA_UID),
                extras.getString(Intent.EXTRA_PACKAGE_NAME),
                extras.getInt(EXTRA_APP_OP_MODE));
        return new Bundle();
    }

    private static Bundle toError(Exception e) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Intent.ACTION_APP_ERROR, e);
        return bundle;
    }

    @FunctionalInterface
    private interface Func {
        Bundle apply(Context context, Bundle extras) throws Exception;
    }

}
