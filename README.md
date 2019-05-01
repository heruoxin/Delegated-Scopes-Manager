# Delegated-Scopes-Manager

## 这是什么？

Android 系统中，设备管理员可以在免 root 状态下提供相当广泛的权限。但是系统限制一台设备上仅能设置一个 App 为设备管理员。为了让一台设备上的诸多 App 能共享权限，此项目应运而生。

## 它能提供什么？

首先它基于系统提供的 DevicePolicyManager.DELEGATION 机制，能提供官方提供的所有权限，包括并不限于：

  [DELEGATION_APP_RESTRICTIONS](https://developer.android.com/reference/android/app/admin/DevicePolicyManager.html#DELEGATION_APP_RESTRICTIONS)
  
  [DELEGATION_BLOCK_UNINSTALL](https://developer.android.com/reference/android/app/admin/DevicePolicyManager.html#DELEGATION_BLOCK_UNINSTALL)
  
  [DELEGATION_CERT_INSTALL](https://developer.android.com/reference/android/app/admin/DevicePolicyManager.html#DELEGATION_CERT_INSTALL)
  
  [DELEGATION_ENABLE_SYSTEM_APP](https://developer.android.com/reference/android/app/admin/DevicePolicyManager.html#DELEGATION_BLOCK_UNINSTALL)
  
  ......
  
然后它也提供各类自定义权限，目前有：

  `dsm-delegation-install-uninstall-app` 安装卸载 App （API >= 26)
  
  `dsm-delegation-set-app-ops` 设置 AppOps 状态（API >= 28)

更多权限和功能有待添加。

## 如何接入使用？

首先请您确认您已经知道设备管理员是什么，以及如何使用等信息。

### 我是设备管理员，想分享权限给其他 App

1. 添加下列依赖：
```groovy
implementation 'com.github.heruoxin.Delegated-Scopes-Manager:manager:master-SNAPSHOT'
```

2. 在您的 `DeviceAdminReceiver#onEnabled` 方法中添加 `CenterApp.getInstance(context).refreshState();` 以便刷新状态。

3. 【推荐】在运行时检测，当 App 当前拥有设备管理员权限时，在适当位置添加一个入口，指向自身的权限授权管理页面，方便用户管理：
```java
startActivity(new Intent(CenterApp.ACTION_APP_LIST).setPackage(context.getPackageName()));
```

### 我是普通 App，想使用设备管理员权限

1. 添加依赖：
```groovy
implementation 'com.github.heruoxin.Delegated-Scopes-Manager:client:master-SNAPSHOT'
```

2. 在 manifests 文件中参考官方托管权限的方式进行注册：
```xml
        <receiver
            android:name="yourAdminReceiver"
            android:permission="android.permission.BIND_DEVICE_ADMIN">
            <intent-filter>
                <action android:name="android.app.action.DEVICE_ADMIN_ENABLED" />
                <!-- 加入以下内容-->
                <action android:name="android.app.develop.action.DEVICE_DELEGATION" />
            </intent-filter>

            <meta-data
                android:name="android.app.develop.delegation"
                android:resource="@xml/app_delegation" />
        </receiver>
```
建立 app_delegation.xml 文件，注册你需要的权限：
```xml
<?xml version="1.0" encoding="utf-8"?>
<device-delegation>
    <uses-policies>
        <!-- 以官方 DevicePolicyManager.DELEGATION_APP_RESTRICTIONS 为例，需要此权限就直接添加其字符串常量 -->
        <delegation-app-restrictions />
        <!-- 上述的自定义权限也可以如此添加，SDK 自定义权限均以 dsm- 开头 -->
        <dsm-delegation-set-app-ops />
    </uses-policies>
</device-delegation>
```

3. 在代码中，判断一下当前设备上有没有可用的已设置管理员的 App
```java
DSMClient.getOwnerSDKVersion(context) >= DSMClient.SDK_VERSION // 判断当前是否有可用的 SDK

String packageName = DSMClient.getOwnerPackageName(context); // 可以获取到当前的设备管理员名称，用于展示给用户
```

4. 检查并申请相关的托管设备管理员权限。
```java
List<String> scopes = DSMClient.getDelegatedScopes(context); // 检查当前已得到哪些托管授权

// 申请权限
DSMClient.requestScopes(activity, DevicePolicyManager.DELEGATION_ENABLE_SYSTEM_APP, "dsm-delegation-install-uninstall-app", ...);
     
// 用户同意授予全部你申请的权限后， `Activity#onActivityResult` 会回调 `RESULT_OK`，否则回调 `RESULT_CANCEL`。
```

5. 对于系统权限，直接调用 DevicePolicyManager 中的相关方法即可，对于自定义权限，请参考 `DSMClient#installApp`，`DSMClient#setAppOpsMode` 等方法调用。

## 目前有哪些 App 支持？

目前作为管理员端支持的有[冰箱 IceBox](https://www.coolapk.com/apk/com.catchingnow.icebox)、[小黑屋](https://www.coolapk.com/apk/web1n.stopapp)。作为客户端支持的有[权限狗](https://www.coolapk.com/apk/com.web1n.permissiondog)。
