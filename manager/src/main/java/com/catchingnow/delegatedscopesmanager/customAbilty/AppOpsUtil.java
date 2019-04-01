package com.catchingnow.delegatedscopesmanager.customAbilty;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.catchingnow.delegatedscopesmanager.util.Hack;

/**
 * @author heruoxin @ CatchingNow Inc.
 * @since 2019-03-31
 */
public class AppOpsUtil {
    private static AppOpsManager sManager;

    @RequiresApi(api = Build.VERSION_CODES.P)
    public static void setMode(Context context, int opCode, int uid, String packageName, int mode) {
        if (sManager == null) {
            sManager = context.getSystemService(AppOpsManager.class);
        }
        Hack.into(AppOpsManager.class)
                .method("setMode")
                .returning(void.class)
                .withParams(int.class, int.class, String.class, int.class)
                .invoke(opCode, uid, packageName, mode)
                .on(sManager);
    }

}
