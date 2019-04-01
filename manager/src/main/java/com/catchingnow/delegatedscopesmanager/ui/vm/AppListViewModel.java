package com.catchingnow.delegatedscopesmanager.ui.vm;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.drawable.Drawable;

/**
 * @author heruoxin @ CatchingNow Inc.
 * @since 2019/3/7
 */
public class AppListViewModel extends BaseObservable {

    @Bindable
    public String name;

    @Bindable
    public String description;

    @Bindable
    public Drawable icon;

}
