package com.xdroid.sqlitelookup.app;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xdroid.sqlitelookup.R;

abstract class BaseActivity extends Activity {

    private ImageView mIvBack;
    private TextView mTvTitle;

    @SuppressWarnings("unchecked")
    public <T extends View> T findView(int id) {
        return (T) findViewById(id);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        Log.d("BaseActivity", "onContentChanged");
        mTvTitle = findView(R.id.tv_title);
        mIvBack = findView(R.id.iv_back);
    }

    protected void setMainTitle(int resId) {
        if (mTvTitle != null) {
            mTvTitle.setText(resId);
        }
    }

    protected void setMainTitle(String title) {
        if (mTvTitle != null) {
            mTvTitle.setText(title);
        }
    }

    protected void enableBack() {
        if (mIvBack != null) {
            mIvBack.setVisibility(View.VISIBLE);
            mIvBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }
}