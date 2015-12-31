package com.chen.scangon.activity;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.chen.scangon.R;
import com.chen.scangon.adapter.ScanAdapter;
import com.chen.scangon.helper.ScanGunKeyEventHelper;

import java.util.ArrayList;

public class MainActivity extends Activity implements ScanGunKeyEventHelper.OnScanSuccessListener {


    private ListView lv_main;
    private TextView tv_main_title_num;
    private ScanAdapter mAdapter;
    private ArrayList<String> mList;
    private ScanGunKeyEventHelper mScanGunKeyEventHelper;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        intData();
    }



    private void initView() {
        lv_main = (ListView) findViewById(R.id.lv_main);
        tv_main_title_num = (TextView) findViewById(R.id.tv_main_title_num);
    }

    private void intData() {
        mContext = this;
        mList = new ArrayList<>();
        mAdapter = new ScanAdapter(mContext, mList);
        lv_main.setAdapter(mAdapter);
        registerBoradcastReceiver();
        mScanGunKeyEventHelper = new ScanGunKeyEventHelper(this);
    }

    /**
     * 截获按键事件.发给ScanGunKeyEventHelper
     *
     * @param event
     * @return
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (mScanGunKeyEventHelper.isScanGunEvent(event)) {
            mScanGunKeyEventHelper.analysisKeyEvent(event);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (!mScanGunKeyEventHelper.hasScanGun()) {
            Toast.makeText(mContext, "未检测到扫码枪设备", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(stateChangeReceiver);
        mScanGunKeyEventHelper.onDestroy();
    }

    @Override
    public void onScanSuccess(String barcode) {
        mList.add(barcode);
        tv_main_title_num.setText("" + mList.size());
        mAdapter.notifyDataSetChanged();
    }



    private void registerBoradcastReceiver() {
        IntentFilter filter1 = new IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED);
        IntentFilter filter2 = new IntentFilter(
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(stateChangeReceiver, filter1);
        registerReceiver(stateChangeReceiver, filter2);
    }

    private BroadcastReceiver stateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                Toast.makeText(MainActivity.this, "蓝牙设备连接状态已变更", Toast.LENGTH_SHORT).show();
            } else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                Toast.makeText(MainActivity.this, "蓝牙设备连接状态已变更", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
