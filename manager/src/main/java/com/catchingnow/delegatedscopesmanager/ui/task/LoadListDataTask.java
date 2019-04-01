package com.catchingnow.delegatedscopesmanager.ui.task;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;

import com.catchingnow.delegatedscopesmanager.ui.adapter.AppListAdapter;
import com.catchingnow.delegatedscopesmanager.centerApp.CenterApp;
import com.catchingnow.delegatedscopesmanager.databinding.ActivityAppListBinding;
import com.catchingnow.delegatedscopesmanager.ui.m.AppListModel;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author heruoxin @ CatchingNow Inc.
 * @since 2019/3/7
 */
public class LoadListDataTask extends AsyncTask<Void, Void, List<AppListModel>> {
    private final CenterApp mCenterApp;
    private final ActivityAppListBinding mBinding;
    private final AppListAdapter mAdapter;

    public LoadListDataTask(ActivityAppListBinding binding, AppListAdapter adapter) {
        super();
        mAdapter = adapter;
        Context context = binding.getRoot().getContext();
        mCenterApp = CenterApp.getInstance(context);
        mBinding = binding;
    }

    @Override
    @MainThread
    protected void onPreExecute() {
        mBinding.setLoading(true);
    }

    @Override
    @WorkerThread
    protected List<AppListModel> doInBackground(Void... voids) {
        ArrayList<AppListModel> models = new ArrayList<>();
        for (ApplicationInfo ai : mCenterApp.getDelegationApps()) {
            try {
                List<String> policies = mCenterApp.getRequireScopes(ai.packageName);
                List<String> delegatedScopes = mCenterApp.getDelegatedScopes(ai.packageName);
                models.add(new AppListModel(ai, policies, delegatedScopes));
            } catch (XmlPullParserException | IOException ignored) {
            }
        }
        return models;
    }

    @Override
    @MainThread
    protected void onPostExecute(List<AppListModel> appListModels) {
        mBinding.setLoading(false);
        mAdapter.fillData(appListModels);
    }

}
