package com.chen.scangon.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;

import com.chen.scangon.helper.ScanGunKeyEventHelper;

import java.util.Iterator;
import java.util.Set;


/**
 * 扫码枪基类
 */
public abstract class ScanGunBaseActivity extends Activity implements ScanGunKeyEventHelper.OnScanSuccessListener {
    final ScanGunKeyEventHelper mScanGunKeyEventHelper = new ScanGunKeyEventHelper();
    private BluetoothAdapter mBluetoothAdapter;
    private String mScanGunName;

    protected Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScanGunKeyEventHelper.setOnBarCodeCatchListener(this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = this;
    }

    /**
     * 截获按键事件.发给ScanGunKeyEventHelper
     *
     * @param event
     * @return
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (isScanGun(event.getDevice().getName())) {
            mScanGunKeyEventHelper.analysisKeyEvent(event);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasScanGunNew()) {
            showScanGunUnBondedDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScanGunKeyEventHelper.onDestroy();
    }

    /**
     * 无扫描枪的提示
     */
    protected abstract void showScanGunUnBondedDialog();

    /**
     * 判断是否有配对的扫描枪
     * @return
     */
    protected boolean hasScanGunNew() {
        Configuration cfg = getResources().getConfiguration();
        if (mBluetoothAdapter == null) {
            return false;
        }

        Set<BluetoothDevice> blueDevices = mBluetoothAdapter.getBondedDevices();

        if (blueDevices == null || blueDevices.size() <= 0) {
            return false;
        }

        for (Iterator<BluetoothDevice> iterator = blueDevices.iterator(); iterator.hasNext(); ) {
            BluetoothDevice bluetoothDevice = iterator.next();

            if (bluetoothDevice.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.PERIPHERAL) {
                mScanGunName = bluetoothDevice.getName();
            }

        }

        return cfg.keyboard != Configuration.KEYBOARD_NOKEYS;
    }


    /**
     * 判断是否是扫描枪(蓝牙输入设备)
     *
     * @param deviceName
     * @return
     */
    private boolean isScanGun(String deviceName) {
        return deviceName.equals(mScanGunName);
    }

    /**
     * 扫描一条条形码成功
     *
     * @param barcode 条形码
     */
    @Override
    public abstract void onScanSuccess(String barcode);

}
