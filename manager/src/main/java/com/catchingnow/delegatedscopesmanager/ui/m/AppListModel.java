package com.catchingnow.delegatedscopesmanager.ui.m;

import android.content.pm.ApplicationInfo;

import java.util.List;

/**
 * @author heruoxin @ CatchingNow Inc.
 * @since 2019/3/7
 */
public class AppListModel {
    public final ApplicationInfo ai;
    public final List<String> policies;
    public final List<String> grantedPolices;

    public AppListModel(ApplicationInfo ai, List<String> policies, List<String> grantedPolices) {
        this.ai = ai;
        this.policies = policies;
        this.grantedPolices = grantedPolices;
    }
}
