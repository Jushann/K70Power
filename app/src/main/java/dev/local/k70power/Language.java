package dev.local.k70power;

import android.content.Context;
import android.content.res.Configuration;
import java.util.Locale;

final class Language {
 static Context context(Context base) {
  boolean english = base.getSharedPreferences("policy", 0).getInt("language", 0) == 1;
  Configuration config = new Configuration(base.getResources().getConfiguration());
  config.setLocale(english ? Locale.ENGLISH : Locale.SIMPLIFIED_CHINESE);
  return base.createConfigurationContext(config);
 }

 static String status(Context ui, String message) {
  if (ui.getResources().getConfiguration().getLocales().get(0).getLanguage().equals("en")) {
   for (String[] pair : STATUS) {
    if (message.startsWith(pair[0])) return pair[1] + message.substring(pair[0].length());
   }
  }
  return message;
 }

 private static final String[][] STATUS = {
  {"未开启控制", "Control is off"},
  {"LSP 未激活，取消自启", "LSPosed inactive. Autostart skipped."},
  {"阈值无效", "Invalid thresholds"},
  {"正在申请 Root…", "Requesting root…"},
  {"数据已更新", "Data updated"},
  {"控制通道断开，等待恢复：", "Control connection lost: "},
  {"Root 启动失败：", "Failed to start root service: "},
  {"当前机型尚未适配：", "Unsupported device: "},
  {"当前内核未提供可读写的充电控制接口：", "No writable charging interface for "},
  {"无法识别 soc_limit 状态：", "Unrecognized soc_limit value: "},
  {"无法识别充电接口状态：", "Unrecognized charging state: "},
  {"当前内核缺少控制所需的读数：", "Required sensor unavailable: "},
  {"恢复充电上限失败", "Could not restore the charge limit"},
  {"已有系统充电上限，已停止控制", "System charge limit active. Control stopped."},
  {"充电接口已由其他控制器使用", "Another controller is using the charging interface"},
  {"系统夜间充电已接管，已停止控制", "System night charging is active. Control stopped."},
  {"写入充电接口失败", "Could not write to the charging interface"},
  {"系统已更改充电接口，已停止控制", "System changed the charging interface. Control stopped."},
 };
}
