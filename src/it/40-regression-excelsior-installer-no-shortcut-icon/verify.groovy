import com.excelsiorjet.TestUtils

String ext = TestUtils.exeExt()
File exeFile = new File(basedir, "target/jet/build/HelloWorld" + ext);
assert exeFile.exists()
exeFile = new File(basedir, "target/jet/app/HelloWorld" + ext)
assert exeFile.exists()
installer = new File(basedir, "target/jet/HelloWorld-1.2.3-SNAPSHOT" + ext)
assert installer.exists();
String xpackArgs = TestUtils.toUnixLineSeparators(new File(basedir, "target/jet/build/HelloWorld.EI.xpack").text)
assert xpackArgs.contains("""-shortcut program-folder README.txt "Read me first" "" "" ""
-file-association abc HelloWorld.exe "ABC Files" "Super Duper Program" "" "" checked
""")
