import com.excelsiorjet.TestUtils

boolean isWindows = TestUtils.isWindows();
String ext = TestUtils.exeExt()
File exeFile = new File( basedir, "target/jet/build/HelloSwing" + ext);
assert exeFile.exists()
exeFile = new File( basedir, "target/jet/app/HelloSwing" + ext)
assert exeFile.exists()
assert !isWindows || new File(basedir, "target/jet/build/HelloSwing_jetpdb/HelloSwing.rsp").text.contains("icon.ico")
assert !isWindows || new File(basedir, "target/jet/build/HelloSwing_jetpdb/HelloSwing.rsp").text.contains("-sys=W")
