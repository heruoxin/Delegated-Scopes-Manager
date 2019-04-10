package com.catchingnow.delegatedscopesmanager.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;

import com.catchingnow.delegatedscopesmanager.R;
import com.catchingnow.delegatedscopesmanager.ui.adapter.AppListAdapter;
import com.catchingnow.delegatedscopesmanager.ui.task.LoadListDataTask;

public class AppListActivity extends AppCompatActivity {
    private AppListAdapter mAdapter;
    private Toolbar mToolbar;
    private RecyclerView mList;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dsm_activity_app_list);
        initView();
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        mList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new AppListAdapter(this);
        mList.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadListDataTask(mProgressBar, mList, mAdapter)
                .execute();
    }

    private void initView() {
        mToolbar = findViewById(R.id.dsm_toolbar);
        mList = findViewById(R.id.dsm_list);
        mProgressBar = findViewById(R.id.dsm_loading);
    }
}
