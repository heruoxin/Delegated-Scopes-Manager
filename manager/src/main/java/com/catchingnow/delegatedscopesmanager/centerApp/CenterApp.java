package com.catchingnow.delegatedscopesmanager.centerApp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

/**
 * @author heruoxin @ CatchingNow Inc.
 * @since 2019/3/8
 */
public interface CenterApp {

    int SDK_VERSION = 1;

    /**
     * ACTION for open the delegation app list.
     *
     *    context.startActivity(new Intent(CenterApp.ACTION_APP_LIST)
     *                         .setPackage(deviceOwnerPackageName));
     *
     */
    String ACTION_APP_LIST = "android.app.develop.action.APP_DELEGATION_LIST";

    /**
     * ACTION for request delegation permissions for specific app.
     *
     *    context.startActivity(new Intent(CenterApp.ACTION_APP_AUTH)
     *                         .putExtra(Intent.EXTRA_PACKAGE_NAME, context.getPackageName())
     *                         .putExtra(CenterApp.ACTION_APP_AUTH_PERMISSIONS, new String[]{
     *                              // optional permissions
     *                         })
     *                         .setPackage(mContext.getPackageName()));
     *
     * You will get RESULT_OK on {@link Activity#onActivityResult} if the user has granted ALL of the permissions.
     * Otherwise RESULT_CANCEL will be returned.
     *
     */
    String ACTION_APP_AUTH = "android.app.develop.action.APP_DELEGATION_AUTH";
    String ACTION_APP_AUTH_PERMISSIONS = "android.app.develop.action.APP_DELEGATION_AUTH_PERMISSIONS";

    static CenterApp getInstance(Context context) {
        return CenterAppInternal.getInstance(context);
    }

    /**
     * Get the list of apps which have declared delegate permissions in their manifests.
     *
     * @return list of apps, no null
     */
    @NonNull
    List<ApplicationInfo> getDelegationApps();

    /**
     * Get the require scopes for specific package name.
     *
     * @param packageName packageName
     * @return scopes, no null
     */
    @NonNull
    List<String> getRequireScopes(String packageName) throws IOException, XmlPullParserException;

    /**
     * Get the delegated scopes for specific package name.
     *
     * @param packageName packageName
     * @return scopes, no null
     */
    @NonNull
    List<String> getDelegatedScopes(String packageName);

    /**
     * Set the delegated scopes for specific package name.
     *
     * @param packageName packageName
     * @param scopes the scopes to delegate.
     */
    @RequiresApi(26)
    void setDelegatedScopes(String packageName, List<String> scopes);

    /**
     * Refresh state. Should be called when device owner state changed.
     */
    void refreshState();

    /**
     * Get human readable name of specific scope
     *
     * @param context context
     * @return name or null
     */
    @Nullable
    String getScopeName(Context context, String scope);
}

