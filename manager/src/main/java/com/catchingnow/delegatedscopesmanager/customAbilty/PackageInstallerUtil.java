package com.catchingnow.delegatedscopesmanager.customAbilty;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author heruoxin @ CatchingNow Inc.
 * @since 2018/11/6
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class PackageInstallerUtil {

    public static boolean installPackage(Context context, Uri uri, @Nullable String packageName) throws IOException {
        try (InputStream in = context.getContentResolver().openInputStream(uri)){

            final AtomicBoolean o = new AtomicBoolean();

            final String name = context.getPackageName()+"_install_"+System.currentTimeMillis();
            Context app = context.getApplicationContext();
            app.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    app.unregisterReceiver(this);
                    int statusCode = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
                    String statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
                    String packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME);
                    o.set(PackageInstaller.STATUS_SUCCESS == statusCode);
                    synchronized (o) {o.notify();}
                }
            }, new IntentFilter(name));

            PackageInstaller packageInstaller = app.getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            if (!TextUtils.isEmpty(packageName)) params.setAppPackageName(packageName);
            // set params
            int sessionId = packageInstaller.createSession(params);
            PackageInstaller.Session session = packageInstaller.openSession(sessionId);
            OutputStream out = session.openWrite(name, 0, -1);
            byte[] buffer = new byte[65536];
            int c;
            while ((c = in.read(buffer)) != -1) {
                out.write(buffer, 0, c);
            }
            session.fsync(out);
            out.close();
            session.commit(createIntentSender(app, sessionId, name));

            synchronized (o) {
                try {
                    o.wait();
                    return o.get();
                } catch (InterruptedException e) {
                    return false;
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    public static boolean uninstallPackage(Context context, String packageName) {
        final AtomicBoolean o = new AtomicBoolean();

        final String name = context.getPackageName()+"_uninstall_" + System.currentTimeMillis();
        Context app = context.getApplicationContext();
        app.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                app.unregisterReceiver(this);
                int statusCode = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
                String statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
                o.set(PackageInstaller.STATUS_SUCCESS == statusCode);
                synchronized (o) {o.notify();}
            }
        }, new IntentFilter(name));

        PackageInstaller mPackageInstaller = app.getPackageManager().getPackageInstaller();
        mPackageInstaller.uninstall(packageName, createIntentSender(app, name.hashCode(), name));

        synchronized (o) {
            try {
                o.wait();
                return o.get();
            } catch (InterruptedException e) {
                return false;
            }
        }
    }

    private static IntentSender createIntentSender(Context context, int sessionId, String name) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                new Intent(name),
                0);
        return pendingIntent.getIntentSender();
    }
}
