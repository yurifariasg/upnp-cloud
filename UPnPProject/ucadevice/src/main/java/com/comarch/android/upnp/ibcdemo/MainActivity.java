package com.comarch.android.upnp.ibcdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @BindView(R.id.temp_bt)
    Button mTempButton;

    @BindView(R.id.body_temp_bt)
    Button mBodyTempButton;

    @BindView(R.id.light_bt)
    Button mLightButton;

    @BindView(R.id.repeat_cb)
    CheckBox mRepeatCb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.temp_bt)
    public void onTempButtonClick() {
        final Intent intent = new Intent(this, TempSensorActivity.class);
        startActivityForResult(intent, 1);
    }

    @OnClick(R.id.body_temp_bt)
    public void onBodyTempButtonClick() {
        final Intent intent = new Intent(this, BodyTempSensorActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.light_bt)
    public void onLightButtonClick() {
        final Intent intent = new Intent(this, SimpleLightActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            restartActivityDelayed();
        }
    }

    private void restartActivityDelayed() {
        mRepeatCb.setChecked(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mRepeatCb.isChecked()) {
                    onTempButtonClick();
                }
            }
        }, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i("result", "Connect XMPP Times");
        Log.i("result", TextUtils.join(",", UtilClass.mStartXMPPTimes));
        Log.i("result", "Disconnect XMPP Times");
        Log.i("result", TextUtils.join(",", UtilClass.mDisconnectXMPPTimes));

    }
}
