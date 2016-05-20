package com.example.relicemxd.runtimepermisstion;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;

public class MainActivity extends AppCompatActivity {
    private final int READ_PHONE_STATE_REQUEST_CODE = 1;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //targetSdk 大于等于23,询问授权
        if (isMNC()) {
            //检查是否需要使用到 READ_PHONE_STATE 权限,如果清单有配置则弹窗询问授权
            int state = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_PHONE_STATE);
            if (state != PackageManager.PERMISSION_GRANTED) {
                //申请 READ_PHONE_STATE 权限
                ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.READ_PHONE_STATE},
                        READ_PHONE_STATE_REQUEST_CODE);
            }
            //直接获取
        } else {
            deviceId = ((TelephonyManager)
                    getSystemService(Context.TELEPHONY_SERVICE))
                    .getDeviceId();
        }
    }

    /**
     * 请求运行时权限requestPermissions 回调
     *
     * @param requestCode 请求码
     * @param permissions 权限数组
     * @param grantResults 返回权限授权状态数组结果, 0为已授权
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_PHONE_STATE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
            } else {
                // Permission Denied
            }
        }
    }

    /**
     * 检查当前 targetSdk是否大于等于23
     */
    private boolean isMNC() {
        return Build.VERSION.SDK_INT >= 23;
    }
}
