package com.catchingnow.delegatedscopesmanager.ui.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.WorkerThread;

import com.catchingnow.delegatedscopesmanager.centerApp.CenterApp;
import com.catchingnow.delegatedscopesmanager.databinding.CardAppListBinding;
import com.catchingnow.delegatedscopesmanager.ui.m.AppListModel;
import com.catchingnow.delegatedscopesmanager.ui.vm.AppListViewModel;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

/**
 * @author heruoxin @ CatchingNow Inc.
 * @since 2019/3/8
 */

public class LoadAppInfoTask extends AsyncTask<AppListModel, AppListViewModel, AppListViewModel> {

    @SuppressLint("StaticFieldLeak")
    private final Context mContext;
    private final CardAppListBinding mBinding;
    private final PackageManager mPm;
    private final CenterApp mCenterApp;

    public LoadAppInfoTask(CardAppListBinding binding) {
        mBinding = binding;
        mContext = mBinding.getRoot().getContext().getApplicationContext();
        mPm = binding.getRoot().getContext().getPackageManager();
        mCenterApp = CenterApp.getInstance(binding.getRoot().getContext());
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected AppListViewModel doInBackground(AppListModel... appListModels) {
        if (appListModels.length == 0) throw new IllegalStateException();
        AppListViewModel app = new AppListViewModel();
        app.name = String.valueOf(appListModels[0].ai.loadLabel(mPm));
        generateDescription(app, appListModels[0].ai);
        publishProgress(app);
        app.icon = appListModels[0].ai.loadIcon(mPm);
        return app;
    }

    @WorkerThread
    private void generateDescription(AppListViewModel app, ApplicationInfo ai) {
        app.name = String.valueOf(ai.loadLabel(mPm));
        try {
        List<String> requireScopes = mCenterApp.getRequireScopes(ai.packageName);
        List<String> delegatedScopes = mCenterApp.getDelegatedScopes(ai.packageName);
        StringBuilder builder = new StringBuilder();
        for (String scope : requireScopes) {
            String flag = delegatedScopes.contains(scope) ? "✓" : "✗";
            String name = mCenterApp.getScopeName(mContext, scope);
            builder.append(name == null ? scope : name)
                    .append("  ")
                    .append(flag)
                    .append("\n");
        }
        app.description = builder.toString();
        } catch (IOException | XmlPullParserException ignore) {}
    }

    @Override
    protected void onProgressUpdate(AppListViewModel... values) {
        mBinding.setApp(values[0]);
        mBinding.getApp().notifyChange();
    }

    @Override
    protected void onPostExecute(AppListViewModel app) {
        mBinding.setApp(app);
        mBinding.getApp().notifyChange();
    }

}
