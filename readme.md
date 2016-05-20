
###What?
####**了解下runtime permission**
2015.8 google发布了android 6.0,sdk版本为23,一款"为工作升级而生"的android系统.如6.0新加入的指纹识别;Doze电量管理;快速充电切换...
还是说本文的重点吧,运行时权限,为了避免一些恶意app行为,如后台流量偷跑,偷偷扣费等情况,google对安全做了进一步的整理和优化.

####**对比android6.0之前有什么区别**
- 在`targetSdkVersion 23`以下时
>对于权限主需要在安装时被询问一次,而且是批量处理的,对于客户而言一般都是很少仔细去看权限的风险内容,
直接安装的,即使在对一些危险权限有红色提醒.但是惯性的操作也解决不了,安全问题.

![image](https://github.com/relice/RunTimePermisstion-master/blob/master/a.png)

- 在`targetSdkVersion 23`以上时
>对于危险权限是需要单独处理的,app在运行时只要接触了危险权限,就会弹窗提醒,询问用户是否授权.

![image](https://github.com/relice/RunTimePermisstion-master/blob/master/b.png)

- 权限管理
>当然你也可以在`setting - apps - xxApp - permissions`中手动开启和关闭对应权限.

![image](https://github.com/relice/RunTimePermisstion-master/blob/master/c.png)


####***6.0对权限的划分***
在整个权限列表内,权限可以分为`normal,dangerous,special类型`其实special也属于`dangerous`类型,但是他的请求方式需要通过,
隐式意图来处理,下面是微信权限和特殊权限的列表
- dangerous permission(危险权限)

![image](https://github.com/relice/RunTimePermisstion-master/blob/master/d.png)

- special permission(特殊权限)
需要通过隐式意图来开启
> *WRITE_SETTINGS*
 *SYSTEM_ALERT_WINDOW*


**************
###Why?
####***runtime permission的出现主要解决什么问题？***
google的更新主要归纳三点:性能的提示,信息的安全,规范的统一.而这次的运行时权限的更新主要就是对信息安全的处理,如6.0之前开发者在AndroidManifest清单文件上申请的权限会被系统默认授权,然而用户如果授权后想反悔取消这些授权,就得通过第三方软件来处理,这样的方式既麻烦也很流氓,还有
比如特殊权限悬浮窗,如果一些开发者利用默认授权的方式,让app一直开启浮窗,这样的体验用户也是不买单的,因此就出现了6.0的`runtime permission`.

**************
###How?
####***什么时候会开启runtime permission？***
- app的gradle配置要求` targetSdkVersion 23`
```gradle
compileSdkVersion 23
    buildToolsVersion "23.0.2"
    defaultConfig {
        targetSdkVersion 23
      ...
    }
```

- 清单文件配置
只有在涉及到危险权限时才会弹窗运行时权限请求
```xml
 <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
```

####***google对涉及到危险权限是怎么处理的呢.***
- 检查当前 targetSdk是否大于等于23
```java
 private boolean isMNC() {
        return Build.VERSION.SDK_INT >= 23;
 }
```

- 检查是否需要使用到 READ_PHONE_STATE 权限,如果清单有配置则弹窗询问授权
```java
  int state = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_PHONE_STATE);
```

- 申请 READ_PHONE_STATE 权限
```java
 ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.READ_PHONE_STATE},
                        READ_PHONE_STATE_REQUEST_CODE);
```

- 请求运行时权限requestPermissions 回调
```java
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
```

<p><a href="http://blog.csdn.net/relicemxd">更多信息可以反问blog ^_^</a>



**************
####***使用第三方RxPermisstion 处理***
- RxPermisstion处理方式
批量处理
```java
RxPermissions.getInstance(this)
    .request(Manifest.permission.READ_PHONE_STATE//DO
            , Manifest.permission.READ_CONTACTS//DO
            , Manifest.permission.GET_ACCOUNTS
            , Manifest.permission.WRITE_CONTACTS
            , Manifest.permission.ACCESS_FINE_LOCATION//DO
            , Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.WRITE_EXTERNAL_STORAGE
            , Manifest.permission.READ_EXTERNAL_STORAGE
            , Manifest.permission.SEND_SMS//DO
            , Manifest.permission.READ_SMS
            , Manifest.permission.RECEIVE_SMS
            , Manifest.permission.CAMERA)//DO
    .subscribe(isGranted -> {
        if (isGranted) {
            System.out.println("全已授权");
        
            doNext(true);
        } else {
       
            System.out.println("没全授权");
        }
    });
```

- 检查这些权限中,有哪些被拒绝,授权
```java
RxPermissions.getInstance(this)
        .requestEach(Manifest.permission.READ_PHONE_STATE
                , Manifest.permission.READ_CONTACTS
                , Manifest.permission.GET_ACCOUNTS
                , Manifest.permission.WRITE_CONTACTS
                , Manifest.permission.ACCESS_FINE_LOCATION
                , Manifest.permission.ACCESS_COARSE_LOCATION
                , Manifest.permission.WRITE_EXTERNAL_STORAGE
                , Manifest.permission.READ_EXTERNAL_STORAGE
                , Manifest.permission.SEND_SMS
                , Manifest.permission.READ_SMS
                , Manifest.permission.RECEIVE_SMS
                , Manifest.permission.CAMERA)
        .subscribe(permission -> {
            if (!permission.granted) {
                System.out.println("denied:" + permission.name);
                SToast.l(mAct, "部分权限未授权,可能会导致app无法正常运行");
            } else {
                System.out.println("granted:" + permission.name);
            }
        });
```

###***在一些特殊权限下需要使用隐式询问授权***
- 这些特殊权限也属于危险权限,但是他们的授权方式与运行时权限不一样需要使用隐式意图开授权.
```java
//运行时权限所需求的弹窗,这边需要先开启运行弹窗权限
@TargetApi(Build.VERSION_CODES.M)
public static void requestAlertPermis(Context mcont, int requestCode) {
    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
    intent.setData(Uri.parse("package:" + mcont.getPackageName()));
    ((Activity) mcont).startActivityForResult(intent, requestCode);
}

//如果项目使用到了推送就需要用到,Write_Settings权限,而它也是属于特殊权限,因此需要隐式开启授权
@TargetApi(Build.VERSION_CODES.M)
public static void requestSettingsPermis(Context mcont, int requestCode) {
    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
    intent.setData(Uri.parse("package:" + mcont.getPackageName()));
    ((Activity) mcont).startActivityForResult(intent, requestCode);
}
```

- 特殊权限的回调处理
```java
 @Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ALEAR_WINDOWS_REQUEST_CODE) {
            if (Settings.canDrawOverlays(mcont)) {//判断是否开启弹窗
               //TODO 请求6.0 所需要的运行时权限
                Toast.l(this, "弹窗权限已开启！");
            } else {
                Toast.l(this, "请开启弹窗权限！");
            }
        } else if (requestCode == WRITE_SETTINGS_REQUEST_CODE) {
           if(Settings.System.canWrite(mcont)){//判断是否开启修改系统
             //TODO 初始化推送
           }else {
              //TODO 其他你想要的处理
            } 
        }
}
```

####实践中遇到的坑
 **异常信息:**
You cannot keep your settings in the secure settings.
**原因: **
该异常是由于`百度推送`和`Writing_Settings`权限引起的,由于android6.0对于一些权限需要特殊处理,如`dangerous permission`,`special permission`,因此在初始化`百度推送`时,需要先开启`Writing_Settings`这个特殊权限,然而开启后还是运行部了,原因是`百度推送`在sdk4.53以下版本,运行在android target为 23时有个坑,也就是这个异常.
**解决方法: **
升级`百度推送`为4.6以上的sdk就可以解决了.*上面有特殊权限处理的代码*


