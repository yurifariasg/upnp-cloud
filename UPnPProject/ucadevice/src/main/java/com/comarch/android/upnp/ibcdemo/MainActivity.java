package com.comarch.android.upnp.ibcdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.temp_bt)
    public void onTempButtonClick() {
        final Intent intent = new Intent(this, TempSensorActivity.class);
        startActivity(intent);
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
}
