import com.excelsiorjet.TestUtils

String ext = TestUtils.exeExt()
File exeFile = new File(basedir, "target/jet/build/HelloWorld" + ext);
assert exeFile.exists()
exeFile = new File(basedir, "target/jet/app/HelloWorld" + ext)
assert exeFile.exists()
installer = new File(basedir, "target/jet/HelloWorld-1.2.3-SNAPSHOT" + ext)
assert installer.exists();
String xpackArgs = new File(basedir, "target/jet/build/HelloWorld.EI.xpack").text
assert xpackArgs.contains("-language german")
assert xpackArgs.contains("-cleanup-after-uninstall")
assert xpackArgs.contains("-compression-level fast")
assert xpackArgs.contains("-after-install-runnable HelloWorld")
assert xpackArgs.contains("arg with space")