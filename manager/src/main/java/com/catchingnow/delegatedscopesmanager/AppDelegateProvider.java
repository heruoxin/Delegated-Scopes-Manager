package com.catchingnow.delegatedscopesmanager;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.catchingnow.delegatedscopesmanager.centerApp.CenterApp;
import com.catchingnow.delegatedscopesmanager.centerApp.CenterAppBridge;

/**
 * @author heruoxin @ CatchingNow Inc.
 * @since 2019-03-31
 */
public class AppDelegateProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        CenterApp.getInstance(getContext()).refreshState();
        return true;
    }

    @Nullable
    @Override
    public Bundle call( @NonNull String method,
                        @Nullable String arg,
                        @Nullable Bundle extras) {
        return CenterAppBridge.call(getContext(), getCallingPackage(), method, arg, extras);
    }

    @Nullable
    @Override
    public Cursor query( @NonNull Uri uri,
                         @Nullable String[] projection,  @Nullable String selection,  @Nullable String[] selectionArgs,  @Nullable String sortOrder) {
        return null;
    }

    
    @Nullable
    @Override
    public String getType( @NonNull Uri uri) {
        return null;
    }

    
    @Nullable
    @Override
    public Uri insert( @NonNull Uri uri,  @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete( @NonNull Uri uri,
                       @Nullable String selection,  @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update( @NonNull Uri uri,
                       @Nullable ContentValues values,
                       @Nullable String selection,  @Nullable String[] selectionArgs) {
        return 0;
    }
}
