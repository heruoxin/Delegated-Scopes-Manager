package com.catchingnow.delegatedscopesmanager.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.catchingnow.delegatedscopesmanager.R;
import com.catchingnow.delegatedscopesmanager.ui.adapter.AppListAdapter;
import com.catchingnow.delegatedscopesmanager.databinding.ActivityAppListBinding;
import com.catchingnow.delegatedscopesmanager.ui.task.LoadListDataTask;

public class AppListActivity extends AppCompatActivity {
    private ActivityAppListBinding mBinding;
    private AppListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_app_list);
        setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        mBinding.list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new AppListAdapter(this);
        mBinding.list.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadListDataTask(mBinding, mAdapter)
                .execute();
    }
}
