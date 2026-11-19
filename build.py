from pathlib import Path
import subprocess,zipfile,os,shutil
base=Path(__file__).resolve().parent
root=base; build=root/'build';build.mkdir(exist_ok=True)
bt=Path(os.environ['BUILD_TOOLS']); sdk=Path(os.environ['ANDROID_JAR']); api=Path(os.environ['XPOSED_API_JAR'])
java=Path(os.environ['JAVA_HOME'])/'bin'
def run(*args):
 print('RUN',Path(str(args[0])).name,flush=True)
 subprocess.run([str(a) for a in args],check=True)
main=root/'app/src/main'
classes=build/'classes';classes.mkdir(exist_ok=True)
sources=list((main/'java').rglob('*.java'))
run(java/'javac.exe','-encoding','UTF-8','-source','8','-target','8','-classpath',str(sdk)+';'+str(api),'-d',classes,*sources)
jar=build/'classes.jar'
with zipfile.ZipFile(jar,'w',zipfile.ZIP_DEFLATED) as z:
 for f in classes.rglob('*.class'):z.write(f,f.relative_to(classes).as_posix())
dex=build/'dex';dex.mkdir(exist_ok=True)
run(java/'java.exe','-cp',bt/'lib/d8.jar','com.android.tools.r8.D8','--min-api','26','--lib',sdk,'--classpath',api,'--output',dex,jar)
unsigned=build/'unsigned.apk'
run(bt/'aapt.exe','package','-f','-M',main/'AndroidManifest.xml','-S',main/'res','-A',main/'assets','-I',sdk,'-F',unsigned)
with zipfile.ZipFile(unsigned,'a',zipfile.ZIP_DEFLATED) as z:
 for f in dex.glob('*.dex'):z.write(f,f.name)
aligned=build/'aligned.apk'
run(bt/'zipalign.exe','-f','-p','4',unsigned,aligned)
key=base/'local-test-key.p12'
if not key.exists():run(java/'keytool.exe','-genkeypair','-keystore',key,'-storepass','local-test-only','-keypass','local-test-only','-alias','k70power','-keyalg','RSA','-keysize','3072','-validity','3650','-dname','CN=K70Power Local Test')
out=base/'out/K70Power-0.11-test.apk';out.parent.mkdir(exist_ok=True)
run(java/'java.exe','-jar',bt/'lib/apksigner.jar','sign','--ks',key,'--ks-pass','pass:local-test-only','--ks-key-alias','k70power','--out',out,aligned)
run(java/'java.exe','-jar',bt/'lib/apksigner.jar','verify','--verbose','--print-certs',out)
run(bt/'zipalign.exe','-c','-v','4',out)
print('BUILT',out)



