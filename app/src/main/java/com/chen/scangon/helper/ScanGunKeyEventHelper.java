package com.chen.scangon.helper;

import android.os.Handler;
import android.view.KeyEvent;

import java.util.ArrayList;


/**
 * 扫码枪事件解析类
 */
public class ScanGunKeyEventHelper {

    private final static long MESSAGE_DELAY = 500;
    ArrayList<String> mBarcodeArray = new ArrayList<String>();
    int mIndex;
    private boolean mCaps;

    private final Runnable mScanningFishedRunnable = new Runnable() {
        @Override
        public void run() {
            performScanSuccess();
        }
    };

    private void performScanSuccess() {
        String barcode = stringBuffer.toString();
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onScanSuccess(barcode);
        stringBuffer.setLength(0);
    }

    Handler mHandler = new Handler();

    private OnScanSuccessListener mOnScanSuccessListener;

    StringBuffer stringBuffer = new StringBuffer();


    public void analysisKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
        if (down) {
            char aChar = 0;
            if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
                aChar = (char) ((mCaps ? 'A' : 'a') + keyCode - KeyEvent.KEYCODE_A);
            } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                aChar = (char) ('0' + keyCode - KeyEvent.KEYCODE_0);
            } else {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_SHIFT_RIGHT:
                    case KeyEvent.KEYCODE_SHIFT_LEFT:
                        mCaps = true;
                        break;
                    case KeyEvent.KEYCODE_PERIOD:
                        aChar = '.';
                        break;
                    case KeyEvent.KEYCODE_MINUS:
                        aChar = mCaps ? '_' : '-';
                        break;
                    case KeyEvent.KEYCODE_SLASH:
                        aChar = '/';
                        break;
                    case KeyEvent.KEYCODE_BACKSLASH:
                        aChar = mCaps ? '|' : '\\';
                        break;
                    case KeyEvent.KEYCODE_ENTER:
                        mHandler.removeCallbacks(mScanningFishedRunnable);
                        mHandler.post(mScanningFishedRunnable);
                    default:
                        return;


                }
            }
            if (aChar != 0) {
                stringBuffer.append(aChar);
            }
            mHandler.removeCallbacks(mScanningFishedRunnable);
            mHandler.postDelayed(mScanningFishedRunnable, MESSAGE_DELAY);
        } else {
            if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) {
                mCaps = false;
            }
        }
    }

    public interface OnScanSuccessListener {
        public void onScanSuccess(String barcode);
    }

    public void setOnBarCodeCatchListener(OnScanSuccessListener onScanSuccessListener) {
        mOnScanSuccessListener = onScanSuccessListener;
    }

    public void onDestroy() {
        mHandler.removeCallbacks(mScanningFishedRunnable);
        mOnScanSuccessListener = null;
    }


}


