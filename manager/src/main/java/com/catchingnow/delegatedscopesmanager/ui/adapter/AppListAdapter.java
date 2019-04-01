package com.catchingnow.delegatedscopesmanager.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.catchingnow.delegatedscopesmanager.centerApp.CenterApp;
import com.catchingnow.delegatedscopesmanager.databinding.CardAppListBinding;
import com.catchingnow.delegatedscopesmanager.ui.m.AppListModel;
import com.catchingnow.delegatedscopesmanager.ui.task.LoadAppInfoTask;
import com.catchingnow.delegatedscopesmanager.ui.vm.AppListViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author heruoxin @ CatchingNow Inc.
 * @since 2019/3/7
 */
public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppListViewHolder> {

    private final List<AppListModel> mModels = new ArrayList<>();
    private final Context mContext;

    public AppListAdapter(Context context) {
        super();
        mContext = context;
    }

    @MainThread
    public void clearData() {
        if (mModels.isEmpty()) return;
        mModels.clear();
        notifyDataSetChanged();
    }

    @MainThread
    public void fillData(List<AppListModel> models) {
        mModels.clear();
        mModels.addAll(models);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new AppListViewHolder(CardAppListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AppListViewHolder vh, int i) {
        AppListModel appListModel = mModels.get(i);
        vh.bind(appListModel);
    }

    @Override
    public int getItemCount() {
        return mModels.size();
    }

    class AppListViewHolder extends RecyclerView.ViewHolder {
        private final CardAppListBinding mBinding;
        private AsyncTask mTask;

        AppListViewHolder(CardAppListBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        private void bind(AppListModel model) {
            if (mTask != null) {
                mTask.cancel(true);
            }
            mTask = new LoadAppInfoTask(mBinding).execute(model);
            mBinding.getRoot().setOnClickListener(v -> {
                mContext.startActivity(new Intent(CenterApp.ACTION_APP_AUTH)
                        .putExtra("android.intent.extra.PACKAGE_NAME", model.ai.packageName)
                        .setPackage(mContext.getPackageName()));
            });
        }

    }
}
