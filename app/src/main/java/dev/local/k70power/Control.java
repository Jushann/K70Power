package dev.local.k70power;
import android.app.*;import android.content.*;import android.os.*;import java.io.*;import java.util.concurrent.*;
public class Control extends Service {
 public static volatile String state="未开启控制";
 public static volatile boolean running=false;
 public static volatile String requested="observe";
 public static volatile String sample=""; public static volatile long sampledAt=0;
 private volatile String mode="observe";
 private volatile int low=30, high=80;
 private java.lang.Process process; private BufferedWriter writer;
 private final ScheduledExecutorService worker=Executors.newSingleThreadScheduledExecutor();
 private volatile boolean stopping=false;
 public IBinder onBind(Intent i){return null;}
 public void onCreate(){super.onCreate(); NotificationManager nm=getSystemService(NotificationManager.class);nm.createNotificationChannel(new NotificationChannel("control","充电控制",NotificationManager.IMPORTANCE_LOW));
 PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,Main.class),PendingIntent.FLAG_IMMUTABLE);
 PendingIntent stop=PendingIntent.getService(this,1,new Intent(this,Control.class).setAction("stop"),PendingIntent.FLAG_IMMUTABLE);
 startForeground(1,new Notification.Builder(this,"control").setSmallIcon(android.R.drawable.ic_lock_idle_charging).setContentTitle("红米电源监测运行中").setContentText("每 2 秒采样；停止后恢复并退出监测").setContentIntent(open).addAction(new Notification.Action.Builder(null,"停止并恢复",stop).build()).setOngoing(true).build());
 }
 public int onStartCommand(Intent i,int flags,int id){
 if(i==null || "stop".equals(i.getAction())){if(i!=null)getSharedPreferences("policy",0).edit().putString("resume_mode","observe").apply();stopSelf();return START_NOT_STICKY;}
 String action=i.getAction();
 boolean boot="boot".equals(action);
 if(boot){
  if(!Activation.active()){state="LSP 未激活，取消自启";stopSelf();return START_NOT_STICKY;}
  action=getSharedPreferences("policy",0).getString("resume_mode","observe");
  android.util.Log.i("K70Boot","Activated; restoring mode="+action);
  getSharedPreferences("policy",0).edit().remove("boot_error").apply();
 }
 if("manual".equals(action))action="auto"; // Migrate legacy saved/manual requests.
 if(!"monitor".equals(action))getSharedPreferences("policy",0).edit().putString("resume_mode",action).apply();
 low=getSharedPreferences("policy",0).getInt("low",30); high=getSharedPreferences("policy",0).getInt("high",80);
 if(low<1||low>75||high<76||high>100){state="阈值无效";stopSelf();return START_NOT_STICKY;}
 if(!"monitor".equals(action)){mode="threshold".equals(action)?"threshold":"auto".equals(action)?"auto":"observe"; requested=mode;}
 if(!running){running=true;state="正在申请 Root…";worker.execute(()->launch());}
 return START_NOT_STICKY;
 }
 private void launch(){try{ if(stopping)return;
 File script=new File(getFilesDir(),"control.sh");try(InputStream in=getAssets().open("control.sh");OutputStream out=new FileOutputStream(script)){byte[] b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);}
 try(InputStream in=getAssets().open("policy.sh");OutputStream out=new FileOutputStream(new File(getFilesDir(),"policy.sh"))){byte[] b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);}
 process=new ProcessBuilder("su","-c","sh "+script.getAbsolutePath()).redirectErrorStream(true).start();
 writer=new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
 new Thread(()->{try(BufferedReader r=new BufferedReader(new InputStreamReader(process.getInputStream()))){String line;while((line=r.readLine())!=null){if(line.startsWith("DATA|")){sample=line;sampledAt=SystemClock.elapsedRealtime();String[] fields=line.split("\\|",-1); if(fields.length>=11 && "observe".equals(fields[1]) && "manual".equals(mode)){mode="observe";requested=mode;} state="数据已更新";}else{state=line;}android.util.Log.i("K70Power",line);}}catch(Exception e){state=e.toString();}finally{running=false;requested="observe";stopSelf();}},"k70-output").start();
 worker.scheduleAtFixedRate(()->{try{if(stopping)return;writer.write(mode+" "+low+" "+high+"\n");writer.flush();}catch(Exception e){state="控制通道断开，等待恢复："+e;stopSelf();}},0,2,TimeUnit.SECONDS);
 }catch(Exception e){state="Root 启动失败："+e;running=false;stopSelf();}}
 public void onDestroy(){stopping=true;worker.execute(()->{try{if(writer!=null){writer.write("stop\n");writer.flush();writer.close();}}catch(Exception ignored){} running=false;});worker.shutdown();super.onDestroy();}
}