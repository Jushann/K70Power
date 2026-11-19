package dev.local.k70power;
import android.content.*;import android.util.Log;import java.util.concurrent.TimeUnit;
public final class BootReceiver extends BroadcastReceiver {
 public void onReceive(Context context,Intent intent){
  if(intent==null||!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))return;
  if(!Activation.active()){Log.i("K70Boot","Skipped: LSPosed hook inactive");return;}
  try{context.startForegroundService(new Intent(context,Control.class).setAction("boot"));Log.i("K70Boot","Boot service requested");}
  catch(RuntimeException e){
   Log.w("K70Boot","System background restriction; trying authorized Root",e);
   final PendingResult pending=goAsync();
   new Thread(()->{java.lang.Process child=null;try{
    if(!Activation.active())return;
    child=new ProcessBuilder("su","-c","am start-foreground-service --user "+(android.os.Process.myUid()/100000)+" -n dev.local.k70power/.Control -a boot").redirectErrorStream(true).start();
    if(!child.waitFor(7,TimeUnit.SECONDS))throw new java.io.IOException("Root start timed out");
    if(child.exitValue()!=0)throw new java.io.IOException("Root start exit="+child.exitValue());
    Log.i("K70Boot","Root boot start command completed");
   }catch(Exception error){Log.e("K70Boot","Boot start failed",error);context.getSharedPreferences("policy",0).edit().putString("boot_error",error.toString()).apply();}
   finally{if(child!=null)child.destroy();pending.finish();}},"k70-boot").start();
  }
 }
}

