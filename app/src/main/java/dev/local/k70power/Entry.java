package dev.local.k70power;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
public final class Entry implements IXposedHookLoadPackage {
 public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p) throws Throwable {
  if(p.packageName.equals("dev.local.k70power")) XposedHelpers.findAndHookMethod("dev.local.k70power.Activation",p.classLoader,"active",XC_MethodReplacement.returnConstant(true));
 }
}