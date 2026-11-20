package dev.local.k70power;
import android.app.*;import android.os.*;import android.content.*;import android.content.res.ColorStateList;import android.graphics.Color;import android.graphics.Typeface;import android.graphics.drawable.GradientDrawable;import android.view.*;import android.widget.*;import android.text.InputType;import java.util.Locale;
public class Main extends Activity {
 public static boolean moduleActive(){return Activation.active();}
 private int BG, CARD, TEXT, MUTED, ACCENT;
 private boolean dark;
 private String[] COLORS, MODES;
 // Background, card and accent in light and dark mode, respectively.
 private static final int[][] PALETTES = {
  {0xfffaf8ff,0xffededf5,0xff52618b,0xff13151c,0xff20232e,0xffb3c7ff},
  {0xfff6faf7,0xffe7f0ea,0xff316751,0xff101815,0xff1c2a24,0xff90d8b5},
  {0xfffbf7ff,0xfff0e8f7,0xff744890,0xff18131c,0xff2b2032,0xffdbb4f2},
  {0xfffff8f4,0xfff6e9e2,0xff904b36,0xff1c1512,0xff30231e,0xfff3b396}
 };
 private int choice(String key,int count){
  int value=prefs().getInt(key,0);
  return value>=0 && value<count ? value : 0;
 }
 private void applyAppearance(){
  int mode=choice("appearance_mode",MODES.length);
  int systemMode=getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
  dark=mode==2 || (mode==0 && systemMode==android.content.res.Configuration.UI_MODE_NIGHT_YES);
  int[] palette=PALETTES[choice("appearance_color",COLORS.length)];
  int offset=dark?3:0;
  BG=palette[offset]; CARD=palette[offset+1]; ACCENT=palette[offset+2];
  TEXT=dark?0xfff3f1f5:0xff303036;
  MUTED=dark?0xffc4c2ca:0xff656570;
  setTheme(dark?android.R.style.Theme_Material_NoActionBar:android.R.style.Theme_Material_Light_NoActionBar);
 }
 private void chooseAppearance(String title,String key,String[] options){
  int current=choice(key,options.length);
  new AlertDialog.Builder(this).setTitle(title).setSingleChoiceItems(options,current,(dialog,index)->{
   dialog.dismiss();
   if(index!=current){
    prefs().edit().putInt(key,index).apply();
    recreate();
   }
  }).setNegativeButton(getString(R.string.cancel),null).show();
 }
 private void appearanceSettings(LinearLayout box){
  section(box,getString(R.string.appearance));LinearLayout panel=card(box);
  button(panel,getString(R.string.mode_prefix)+MODES[choice("appearance_mode",MODES.length)],()->chooseAppearance(getString(R.string.display_mode),"appearance_mode",MODES));
  button(panel,getString(R.string.color_prefix)+COLORS[choice("appearance_color",COLORS.length)],()->chooseAppearance(getString(R.string.theme_color),"appearance_color",COLORS));
 }

 private TextView status,battery,usb,rule,supply,cpu;private EditText lower,upper;private Switch enable,threshold;private boolean syncing=false;private final Handler handler=new Handler();
 private final Runnable refresh=new Runnable(){public void run(){render();handler.postDelayed(this,1000);}};
 private android.content.SharedPreferences prefs(){return getSharedPreferences("policy",0);}
 private String number(String s,double scale,String unit){try{return String.format(Locale.CHINA,"%.3f %s",Double.parseDouble(s)/scale,unit);}catch(Exception e){return "—";}}
 private String power(String v,String i){try{return String.format(Locale.CHINA,"%.2f W",Double.parseDouble(v)*Double.parseDouble(i)/1e12);}catch(Exception e){return "—";}}
 private void render(){
 syncing=true;enable.setChecked(Control.running&&!"observe".equals(Control.requested));syncing=false;
 String[] f=Control.sample.split("\\|",-1);long age=Control.sampledAt==0?-1:(SystemClock.elapsedRealtime()-Control.sampledAt)/1000;
 String detail=getString(R.string.waiting_device);cpu.setText(getString(R.string.cpu_wait));
 supply.setText(getString(R.string.bypass_wait));
 if(f.length>=11){boolean stale=age>6||!Control.running;String tag=stale?getString(R.string.history):"";
 supply.setText(getString(R.string.bypass)+(stale?getString(R.string.expired):("1".equals(f[2])&&"1".equals(f[3])?getString(R.string.yes):getString(R.string.no))));
 cpu.setText(f.length>=13&&!f[11].isEmpty()?getString(R.string.cpu_peak)+number(f[11],1000,"°C")+tag+getString(R.string.sensor)+f[12]:getString(R.string.cpu_unavailable));
 battery.setText(getString(R.string.battery)+tag+"\n"+number(f[4],1e6,"V")+"   ·   "+number(f[5],1000,"mA")+getString(R.string.net_power)+power(f[4],f[5])+getString(R.string.level)+f[8]+getString(R.string.temperature)+number(f[9],10,"°C"));
 usb.setText(getString(R.string.usb)+tag+"\n"+number(f[6],1e6,"V")+"   ·   "+number(f[7],1000,"mA")+getString(R.string.input_power)+("0".equals(f[3])?getString(R.string.unplugged):power(f[6],f[7])));
 detail=("1".equals(f[2])?getString(R.string.requested):getString(R.string.normal))+"\nUSB "+("1".equals(f[3])?getString(R.string.connected):getString(R.string.disconnected))+" · "+age+getString(R.string.seconds_ago)+(stale?getString(R.string.stale):"");}
 status.setText(detail+"\n"+Language.status(this, Control.state)+getString(R.string.root_service)+(Control.running?getString(R.string.running):getString(R.string.stopped))+" · LSPosed "+(moduleActive()?getString(R.string.active):getString(R.string.inactive)));
 }
 @Override protected void attachBaseContext(Context base){super.attachBaseContext(Language.context(base));}
 public void onCreate(Bundle b){ COLORS = new String[]{getString(R.string.mist), getString(R.string.pine), getString(R.string.wisteria), getString(R.string.clay)};
 MODES = new String[]{getString(R.string.system_mode), getString(R.string.light), getString(R.string.dark)};
applyAppearance();super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);getWindow().getDecorView().setSystemUiVisibility(dark?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
 LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(BG);if(Build.VERSION.SDK_INT>=29)root.setForceDarkAllowed(false);root.setOnApplyWindowInsetsListener((v,in)->{v.setPadding(in.getSystemWindowInsetLeft(),in.getSystemWindowInsetTop(),in.getSystemWindowInsetRight(),in.getSystemWindowInsetBottom());return in.consumeSystemWindowInsets();});
 powerPage = new ScrollView(this);
 settingsPage = new ScrollView(this);
 LinearLayout box = addPage(root, powerPage, 0x31001);
 LinearLayout settings = addPage(root, settingsPage, 0x31004);
 setContentView(root);
 root.requestApplyInsets();
 label(box,getString(R.string.app_title),34,TEXT);label(box,deviceName()+getString(R.string.subtitle),16,MUTED);
 section(box,getString(R.string.live_power));LinearLayout c=card(box);battery=label(c,getString(R.string.battery_wait),19,TEXT);cpu=label(c,getString(R.string.cpu_wait),16,TEXT);supply=label(c,getString(R.string.bypass_unknown),16,dark?0xff92baff:0xff2563eb);label(c,getString(R.string.hardware_note),12,MUTED);line(c);usb=label(c,getString(R.string.usb_wait),19,TEXT);
 section(box,getString(R.string.status));c=card(box);status=label(c,getString(R.string.connecting),16,TEXT);
 label(c,getString(R.string.boot)+(Activation.active()?getString(R.string.boot_active):getString(R.string.boot_inactive)),14,ACCENT);
 section(box,getString(R.string.controls));c=card(box);enable=toggle(c,getString(R.string.master),getString(R.string.master_hint));
 label(settings,getString(R.string.settings),34,TEXT);
 button(settings,getString(R.string.language_label),()->chooseAppearance(getString(R.string.language_title),"language",new String[]{"简体中文","English"}));
 section(settings,getString(R.string.thresholds));c=card(settings);threshold=toggle(c,getString(R.string.range_toggle),getString(R.string.range_hint));
 threshold.setChecked(prefs().getBoolean("threshold",true));
 line(c);label(c,getString(R.string.resume_at),18,TEXT);lower=input(c,prefs().getInt("low",30),"1–75");label(c,getString(R.string.bypass_at),18,TEXT);upper=input(c,prefs().getInt("high",80),"76–100");lower.setId(0x31002);upper.setId(0x31003);rule=label(c,"",14,MUTED);updateRule();button(c,getString(R.string.save),()->save());
 label(c,getString(R.string.cycle_hint),14,MUTED);
 
 appearanceSettings(settings);
 section(settings,getString(R.string.monitor_info));c=card(settings);button(c,getString(R.string.monitor_start),()->start("monitor"));button(c,getString(R.string.monitor_stop),()->{prefs().edit().putString("resume_mode","observe").apply();stopService(new Intent(this,Control.class));});
 label(c,getString(R.string.notes),14,MUTED);
 enable.setOnCheckedChangeListener((v,on)->{if(!syncing)start(on?selected():"observe");});
 threshold.setOnCheckedChangeListener((v,on)->{prefs().edit().putBoolean("threshold",on).apply();if(enable.isChecked())start(selected());});
 label(settings,getString(R.string.version),13,MUTED).setGravity(Gravity.CENTER);
 LinearLayout nav = new LinearLayout(this);
 nav.setPadding(dp(12), dp(4), dp(12), dp(4));
 root.addView(nav);
 powerTab = addTab(nav, getString(R.string.power), false);
 settingsTab = addTab(nav, getString(R.string.settings), true);
 showSettings(b != null && b.getBoolean("settings_page"));
 if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=0)requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},7);
 if(b==null)start("monitor");
 }
 private ScrollView powerPage, settingsPage;
 private Button powerTab, settingsTab;
 private boolean viewingSettings;

 private LinearLayout addPage(LinearLayout root, ScrollView page, int id) {
  page.setId(id);
  page.setFillViewport(true);
  LinearLayout content = new LinearLayout(this);
  content.setOrientation(LinearLayout.VERTICAL);
  content.setFocusableInTouchMode(true);
  content.setPadding(dp(20), dp(36), dp(20), dp(24));
  page.addView(content);
  root.addView(page, new LinearLayout.LayoutParams(-1, 0, 1));
  return content;
 }

 private Button addTab(LinearLayout bar, String title, boolean settings) {
  Button tab = new Button(this);
  tab.setText(title);
  tab.setTextSize(16);
  tab.setOnClickListener(view -> showSettings(settings));
  bar.addView(tab, new LinearLayout.LayoutParams(0, dp(48), 1));
  return tab;
 }

 private void paintTab(Button tab, boolean selected) {
  tab.setSelected(selected);
  tab.setBackground(bg(selected ? CARD : BG, 16));
  tab.setTextColor(selected ? ACCENT : MUTED);
  tab.setTypeface(Typeface.DEFAULT, selected ? Typeface.BOLD : Typeface.NORMAL);
 }

 private void showSettings(boolean settings) {
  if (viewingSettings && !settings) {
   getSystemService(android.view.inputmethod.InputMethodManager.class)
    .hideSoftInputFromWindow(settingsPage.getWindowToken(), 0);
  }
  viewingSettings = settings;
  powerPage.setVisibility(settings ? View.GONE : View.VISIBLE);
  settingsPage.setVisibility(settings ? View.VISIBLE : View.GONE);
  paintTab(powerTab, !settings);
  paintTab(settingsTab, settings);
 }

 @Override protected void onSaveInstanceState(Bundle state) {
  super.onSaveInstanceState(state);
  state.putBoolean("settings_page", viewingSettings);
 }
 private String deviceName() {
  switch (Build.DEVICE) {
   case "manet": return "Redmi K70 Pro";
   case "socrates": return getString(R.string.socrates);
   case "myron": return getString(R.string.myron);
   case "peridot": return getString(R.string.peridot);
   default: return Build.MODEL;
  }
 }
 private String selected(){return threshold.isChecked()?"threshold":"auto";}
 private void save(){try{int n=Integer.parseInt(lower.getText().toString()),m=Integer.parseInt(upper.getText().toString());if(n<1||n>75){lower.setError(getString(R.string.invalid_low));return;}if(m<76||m>100){upper.setError(getString(R.string.invalid_high));return;}prefs().edit().putInt("low",n).putInt("high",m).apply();updateRule();if(enable.isChecked())start(selected());Toast.makeText(this,getString(R.string.saved),Toast.LENGTH_SHORT).show();}catch(NumberFormatException e){Toast.makeText(this,getString(R.string.invalid_number),Toast.LENGTH_SHORT).show();}}
 private void updateRule(){rule.setText(getString(R.string.rule_start)+prefs().getInt("low",30)+getString(R.string.rule_middle)+prefs().getInt("high",80)+getString(R.string.rule_end));}
 private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
 private GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 private TextView label(LinearLayout p,String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);t.setPadding(0,dp(7),0,dp(9));t.setLineSpacing(dp(3),1);p.addView(t,new LinearLayout.LayoutParams(-1,-2));return t;}
 private void section(LinearLayout p,String s){TextView t=label(p,s,17,ACCENT);t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(10),dp(22),0,dp(10));}
 private LinearLayout card(LinearLayout p){LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(18),dp(12),dp(18),dp(12));c.setBackground(bg(CARD,22));p.addView(c,new LinearLayout.LayoutParams(-1,-2));return c;}
 private void line(LinearLayout p){View v=new View(this);v.setBackgroundColor(dark?0xff50505a:0xffceced8);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(1));lp.setMargins(0,dp(12),0,dp(12));p.addView(v,lp);}
 private Switch toggle(LinearLayout p,String title,String sub){Switch s=new Switch(this);s.setText(title);s.setTextSize(19);s.setTextColor(TEXT);s.setPadding(0,dp(8),0,dp(8));s.setTrackTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{ACCENT,0xffa9a9b8}));s.setThumbTintList(ColorStateList.valueOf(BG));p.addView(s,new LinearLayout.LayoutParams(-1,-2));label(p,sub,14,MUTED);return s;}
 private EditText input(LinearLayout p,int value,String hint){EditText e=new EditText(this);e.setInputType(InputType.TYPE_CLASS_NUMBER);e.setSingleLine(true);e.setText(String.valueOf(value));e.setHint(hint);e.setTextColor(TEXT);e.setHintTextColor(MUTED);e.setTextSize(22);e.setPadding(dp(14),dp(10),dp(14),dp(10));e.setBackground(bg(BG,12));p.addView(e,new LinearLayout.LayoutParams(-1,-2));return e;}
 private void button(LinearLayout p,String title,Runnable action){Button b=new Button(this);b.setText(title);b.setAllCaps(false);b.setTextColor(dark?BG:Color.WHITE);b.setTextSize(16);b.setBackground(bg(ACCENT,18));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(50));lp.setMargins(0,dp(10),0,dp(6));p.addView(b,lp);b.setOnClickListener(v->action.run());}
 void start(String action){startForegroundService(new Intent(this,Control.class).setAction(action));}
 protected void onResume(){super.onResume();handler.post(refresh);}protected void onPause(){handler.removeCallbacks(refresh);super.onPause();}
}
