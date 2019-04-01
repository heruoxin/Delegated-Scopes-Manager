package com.catchingnow.delegatedscopesmanager.ui;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.catchingnow.delegatedscopesmanager.R;
import com.catchingnow.delegatedscopesmanager.centerApp.CenterApp;
import com.catchingnow.delegatedscopesmanager.databinding.ActivityAppAuthBinding;
import com.catchingnow.delegatedscopesmanager.databinding.CardAppPermissionBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author heruoxin @ CatchingNow Inc.
 * @since 2019/3/12
 */

@RequiresApi(api = Build.VERSION_CODES.O)
public class AppAuthActivity extends AppCompatActivity {

    private ActivityAppAuthBinding mBinding;
    private CenterApp mCenterApp;
    private String packageName;
    private HashSet<String> delegatedScopes;
    @Nullable
    private String[] permissions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            finish();
            return;
        }
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_app_auth);
        mCenterApp = CenterApp.getInstance(this);
        mBinding.btnOk.setOnClickListener(v -> {
            if (packageName != null && delegatedScopes != null) {
                if (permissions == null) {
                    setResult(RESULT_OK);
                } else {
                    int result = RESULT_OK;
                    for (String p : permissions) {
                        if (!delegatedScopes.contains(p)) {
                            result = RESULT_CANCELED;
                            break;
                        }
                    }
                    setResult(result);
                }
            }
            onBackPressed();
        });
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveScopes();
        finish();
    }

    private void saveScopes() {
        if (packageName != null && delegatedScopes != null) {
            mCenterApp.setDelegatedScopes(packageName, new ArrayList<>(delegatedScopes));
        }
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        packageName = intent.getStringExtra("android.intent.extra.PACKAGE_NAME");
        permissions = intent.getStringArrayExtra(CenterApp.ACTION_APP_AUTH_PERMISSIONS);
        if (TextUtils.isEmpty(packageName)) return;
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(packageName, 0);
            mBinding.appName.setText(ai.loadLabel(getPackageManager()));
            mBinding.appIcon.setImageDrawable(ai.loadIcon(getPackageManager()));

            List<String> requireScopes = mCenterApp.getRequireScopes(packageName);
            delegatedScopes = new HashSet<>(mCenterApp.getDelegatedScopes(packageName));
            mBinding.permissions.removeAllViews();
            for (String scope : requireScopes) {
                CardAppPermissionBinding childBinding = CardAppPermissionBinding
                        .inflate(LayoutInflater.from(this), mBinding.permissions, true);
                //noinspection ConstantConditions
                String scopeName = mCenterApp.getScopeName(this, scope);
                childBinding.name.setText(TextUtils.isEmpty(scopeName) ? scope : scopeName);
                childBinding.check.setChecked(delegatedScopes.contains(scope));
                childBinding.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        delegatedScopes.add(scope);
                    } else {
                        delegatedScopes.remove(scope);
                    }
                    saveScopes();
                });
            }
        } catch (Exception ignore) {
            Toast.makeText(this, R.string.toast_failure_read_app, Toast.LENGTH_SHORT)
                    .show();
            finish();
        }
    }

}
