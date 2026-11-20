# LSPosed bypass charging module for Redmi phones

[简体中文](README.md) · **English**

This module controls battery charging while a Redmi phone is plugged in. In threshold mode, it requests bypass when the battery reaches the upper limit (M), including when you plug in above that limit. Charging resumes when the battery falls to the lower limit (N). For example, with N at 30% and M at 85%, the phone requests bypass at 85% and resumes charging at 30%.

## Device support

| Device | Codename | Verification |
| --- | --- | --- |
| Redmi K70 Pro | `manet` | Installed on a physical device running Android 16, HyperOS 3 and LSPosed 2.0.2. |
| Redmi K90 Pro Max | `myron` | Experimental, tested with a simulated driver. Uses the vendor’s `soc_limit` interface and yields to an existing system charge limit or active night charging. [Driver reference](https://github.com/QiZhi-yi/bypass-charger) |
| Redmi K60 Pro | `socrates` | Experimental, tested with simulated charging nodes. [Device-specific charging reference](https://github.com/QiZhi-yi/bypass-charger) |
| Redmi Turbo 3 / POCO F6 | `peridot` | Experimental, tested with simulated charging nodes. Some ROMs report that bypass works only above 80%. [ROM changelog](https://github.com/poco-f6-peridot/Updater-Stuff-QPR2/blob/main/changelog_peridot.txt) |

Kernel support can vary even on the same model. The module checks the device codename, charging interface permissions and sensor readings. If a required interface is unavailable, it stops control and displays the reason.

## Setup and monitoring

The app opens in Simplified Chinese by default. To switch to English, open 设置 (Settings), tap 界面语言 (Language), then select English. Your choice is saved.

1. Install the APK and grant the app root access in KernelSU.
2. Open Settings (设置) and set the lower threshold N (1–75%) and upper threshold M (76–100%). Enable the bypass control switch (直供电总开关).
3. To start the module at boot, enable it in LSPosed and select the module’s own app in its scope.

The live power card shows battery and USB voltage, current and estimated power, plus the hottest reported CPU thermal zone. The bypass indicator reflects the charging driver’s control state. Confirming the physical power path requires hardware measurement.

## Build the 0.13 test APK on Windows

Install Python 3, JDK 17, Android SDK Platform 36 and Android SDK Build Tools 36. Set the following environment variables:

| Variable | Value |
| --- | --- |
| `JAVA_HOME` | JDK installation directory |
| `ANDROID_JAR` | Path to Platform 36’s `android.jar` |
| `BUILD_TOOLS` | Build Tools directory containing `aapt.exe` |
| `XPOSED_API_JAR` | Path to the downloaded [Xposed API 82 JAR](https://api.xposed.info/repo/de/robv/android/xposed/api/82/api-82.jar) |

Run `python build.py` from the repository root. The script creates a local test signing key on the first build and writes the APK to `out/`. To install an update over an existing copy, sign it with the same key.

## License

The source code is available under the [MIT License](LICENSE).
