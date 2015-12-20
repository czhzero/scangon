条形码扫码枪现在随处可见，可以很迅速地扫描出条形码内容，比什么手机相机扫码快了不是一点两点。
为了节约成本，扫码枪可以直接通过蓝牙连接android或其他设备。
那么android设备如何通过蓝牙获取扫描内容的呢？

--------


###  1. 蓝牙配对，连接设备 ###

打开系统设置，找到蓝牙，打开扫码枪，配对扫码枪设备。输入一个固定的配对码，一般扫码枪说明书里都有写。配对完成后，显示设备已连接。就ok。

###  2.AndroidManifest中配置权限 ###

android项目中的AndroidManifest.xml文件添加蓝牙权限。
	
```
    <uses-permission     android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
```

###  3.检测扫码枪的连接状态 ###

通常来说，扫码枪设备也相当于普通外接输入设备类型，外接键盘。

我这款扫码枪设备返回的是如下蓝牙类型。

> BluetoothClass.Device.Major.PERIPHERAL

一般而言，通过如下这种方式就可以获得到我们扫码枪设备的信息。

```
Set<BluetoothDevice> blueDevices = mBluetoothAdapter.getBondedDevices();

if (blueDevices == null || blueDevices.size() <= 0) {
    return false;
}

for (Iterator<BluetoothDevice> iterator = blueDevices.iterator(); iterator.hasNext(); ) {
    BluetoothDevice bluetoothDevice = iterator.next();

    if (bluetoothDevice.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.PERIPHERAL) {
        //TODO 获取扫码枪设备信息
    }

}
```

开发过程中，必然会需要实时判断设备是否正常连接。

>mBluetoothAdapter.getBondedDevices()

这个方法仅仅只能够判断设备是否已配对绑定。但是绑定不代表连接，所以只能放弃。

>public List<BluetoothDevice> getConnectedDevices (int profile)
>public int getConnectionState (BluetoothDevice device, int profile)

接着又尝试了这两个方法，方法是可用，但是必须要求设备sdk>18,即android 4.3版本以上才可用。

后来转头一想，既然扫码枪也是输入设备，我们可以不同蓝牙设备状态检测入手，改为从输入设备检测入手。于是，

```
private void hasScanGun() {
	Configuration cfg = getResources().getConfiguration();
	return cfg.keyboard != Configuration.KEYBOARD_NOKEYS;
}
```

搞定。

###  4.获取扫码枪扫描内容 ###

扫描枪，既然是一个外接输入设备，那么很自然的，我们就从KeyEvent入手。

事件解析类
```
/**
 * 扫码枪事件解析类
 */
public class ScanGunKeyEventHelper {

    //延迟500ms，判断扫码是否完成。
    private final static long MESSAGE_DELAY = 500;
    //扫码内容
    private StringBuffer mStringBufferResult = new StringBuffer();
    //大小写区分
    private boolean mCaps;
    private OnScanSuccessListener mOnScanSuccessListener;
    private Handler mHandler = new Handler();

    private final Runnable mScanningFishedRunnable = new Runnable() {
        @Override
        public void run() {
            performScanSuccess();
        }
    };

    //返回扫描结果
    private void performScanSuccess() {
        String barcode = mStringBufferResult.toString();
        if (mOnScanSuccessListener != null)
            mOnScanSuccessListener.onScanSuccess(barcode);
        mStringBufferResult.setLength(0);
    }

    //key事件处理
    public void analysisKeyEvent(KeyEvent event) {

        int keyCode = event.getKeyCode();

        //字母大小写判断
        checkLetterStatus(event);

        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            char aChar = getInputCode(event);;

            if (aChar != 0) {
                mStringBufferResult.append(aChar);
            }

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                //若为回车键，直接返回
                mHandler.removeCallbacks(mScanningFishedRunnable);
                mHandler.post(mScanningFishedRunnable);
            } else {
                //延迟post，若500ms内，有其他事件
                mHandler.removeCallbacks(mScanningFishedRunnable);
                mHandler.postDelayed(mScanningFishedRunnable, MESSAGE_DELAY);
            }

        }
    }

    //检查shift键
    private void checkLetterStatus(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                //按着shift键，表示大写
                mCaps = true;
            } else {
                //松开shift键，表示小写
                mCaps = false;
            }
        }
    }


    //获取扫描内容
    private char getInputCode(KeyEvent event) {

        int keyCode = event.getKeyCode();

        char aChar;

        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            //字母
            aChar = (char) ((mCaps ? 'A' : 'a') + keyCode - KeyEvent.KEYCODE_A);
        } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            //数字
            aChar = (char) ('0' + keyCode - KeyEvent.KEYCODE_0);
        } else {
            //其他符号
            switch (keyCode) {
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
                default:
                    aChar = 0;
                    break;
            }
        }

        return aChar;

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

```


Activity中重写dispatchKeyEvent方法，截取Key事件。

```
 /**
     * Activity截获按键事件.发给ScanGunKeyEventHelper
     *
     * @param event
     * @return
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (isScanGunEvent(event)) {
	         mScanGunKeyEventHelper.analysisKeyEvent(event);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 显示扫描内容
     * @param barcode 条形码
     */
    @Override
    public void onScanSuccess(String barcode) {
	    //TODO 显示扫描内容
    }
```


详细代码参看：https://github.com/czhzero/scangon