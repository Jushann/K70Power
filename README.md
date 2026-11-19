# 基于Lspose的直供电模块

适用于红米 K70 Pro（manet）的充电控制与电源监测工具，已在 Android 16、澎湃 OS 3 和 LSPosed 2.0.2 环境测试。

开启直供电总开关后，可按自定义电量区间控制充电。电量达到上限 M 时请求旁路，降至下限 N 时恢复充电。实时电源卡显示电池与 USB 输入侧的电压、电流、估算功率及 CPU 热区温度。

充电控制需要 Root 权限，通过 `night_charging` 节点请求暂停充电。界面中的直供电状态反映节点读数，实际供电路径仍需硬件测量确认。
## 构建 0.10 测试版

在 Windows 上安装 Python 3、JDK 17、Android SDK Platform 36 和 Android SDK Build Tools 36。准备 [Xposed API 82](https://api.xposed.info/repo/de/robv/android/xposed/api/82/api-82.jar) 作为编译依赖。

将环境变量 `JAVA_HOME` 指向 JDK，`ANDROID_JAR` 指向 Platform 36 的 android.jar，`BUILD_TOOLS` 指向包含 aapt.exe 的构建工具目录，`XPOSED_API_JAR` 指向下载的 API JAR。在项目根目录执行：

```powershell
python build.py
```

APK 输出到 out/。脚本首次运行时生成本地测试签名。自行构建的 APK 使用独立签名，覆盖安装需要与已安装版本使用同一签名。

## 使用

安装后在 KernelSU 中授予应用 Root 权限，打开「直供电总开关」。在设置页可选择电量区间，N 为 1–75，M 为 76–100。监测页显示 USB 与电池功率、CPU 最高热区和旁路请求状态。需要开机自启时，在 LSPosed 中启用模块并勾选本应用作用域。

## 许可证

项目源码采用 MIT 许可证，见 LICENSE。
