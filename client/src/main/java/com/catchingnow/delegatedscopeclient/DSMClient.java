package com.catchingnow.delegatedscopeclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import java.util.List;

/**
 * @author heruoxin @ CatchingNow Inc.
 * @since 2019-04-01
 */
public class DSMClient {
    public static final int SDK_VERSION = 2;

    public static final String ACTION_LIST = "android.app.develop.action.APP_DELEGATION_LIST";
    public static final String ACTION_REQUEST_AUTH = "android.app.develop.action.APP_DELEGATION_AUTH";
    public static final String ACTION_REQUEST_AUTH_PERMISSIONS = "android.app.develop.action.APP_DELEGATION_AUTH_PERMISSIONS";

    public static final class Scopes {
        @RequiresApi(api = Build.VERSION_CODES.O)
        public static final String INSTALL_UNINSTALL_APP = "dsm-delegation-install-uninstall-app";

        @RequiresApi(api = Build.VERSION_CODES.P)
        public static final String SET_APP_OPS = "dsm-delegation-set-app-ops";
    }

    /**
     * Get the package name of current device owner or profile owner.
     *
     * @param context context
     * @return package name of the device owner or profile owner
     */
    public static String getOwnerPackageName(Context context) {
        return DSMClinetImplement.getOwnerPackageName(context);
    }

    /**
     * Get the SDK version of owner.
     * see {@link DSMClient#SDK_VERSION}
     *
     * @param context context
     * @return owner SDK version. -1 for not found.
     */
    public static int getOwnerSDKVersion(Context context) {
        return DSMClinetImplement.getOwnerSDKVersion(context);
    }

    /**
     * Get the delegated scopes for yourself.
     *
     * @param context context
     * @return scopes, no null
     */
    @NonNull
    public static List<String> getDelegatedScopes(Context context) {
        return DSMClinetImplement.getDelegatedScopes(context);
    }

    /**
     * Request the scopes.
     *
     * You will get RESULT_OK on {@link Activity#onActivityResult(int, int, Intent)} if the user has granted ALL of the permissions.
     * Otherwise RESULT_CANCEL will be returned.
     *
     * @param activity activity context
     * @param scopes the scopes your app required
     */
    public static void requestScopes(Activity activity, int requestCode, String... scopes) {
        DSMClinetImplement.requestScopes(activity, requestCode, scopes);
    }

    /**
     * Use {@link DSMClient#requestScopes(Activity, int, String...)} instead.
     */
    @Deprecated
    public static void requestScopes(Context activity, String... scopes) {
        DSMClinetImplement.requestScopes(activity, scopes);
    }

    // ---------------------------

    /**
     * Install apk.
     *
     * @param context context
     * @param apkUri uri to the apk file
     * @param packageName packageName, optional
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void installApp(Context context, Uri apkUri, @Nullable String packageName) throws Exception {
         DSMClinetImplement.installApp(context, apkUri, packageName);
    }

    /**
     * Uninstall app.
     *
     * @param context context
     * @param packageName packageName
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void uninstallApp(Context context, String packageName) throws Exception {
        DSMClinetImplement.uninstallApp(context, packageName);
    }

    /**
     *
     * Set the AppOps state.
     *
     * The device owner can only SET the AppOps states but can not GET the AppOps states.
     * For get the AppOps state you have to register the "android.permission.GET_APP_OPS_STATS" permission in manifests.
     * Then ask user to grant it manually through ADB command:
     *
     *     adb shell pm grant com.your.package android.permission.GET_APP_OPS_STATS
     *
     * @param context context
     * @param opCode code
     * @param uid udi
     * @param packageName packageName
     * @param mode mode
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    public static void setAppOpsMode(Context context, int opCode, int uid, String packageName, int mode) throws Exception {
        DSMClinetImplement.setAppOpsMode(context, opCode, uid, packageName, mode);
    }

}
