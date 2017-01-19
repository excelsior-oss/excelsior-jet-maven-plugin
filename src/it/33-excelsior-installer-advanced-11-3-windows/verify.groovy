import com.excelsiorjet.TestUtils

String ext = TestUtils.exeExt()
File exeFile = new File(basedir, "target/jet/build/HelloWorld" + ext);
assert exeFile.exists()
exeFile = new File(basedir, "target/jet/app/HelloWorld" + ext)
assert exeFile.exists()
installer = new File(basedir, "target/jet/HelloWorld-1.2.3-SNAPSHOT" + ext)
assert installer.exists();
String xpackArgs = TestUtils.toUnixLineSeparators(new File(basedir, "target/jet/build/HelloWorld.EI.xpack").text)
assert xpackArgs.contains("-registry-key excelsior/maven/tests")
assert xpackArgs.contains("""-shortcut program-folder README.txt "Read me first" /readme.ico "" ""
-no-default-post-install-actions 
-post-install-checkbox-run run.bat workDir arg checked
-post-install-checkbox-open README.txt checked
-post-install-checkbox-restart unchecked
-file-association abc HelloWorld.exe "ABC Files" "Super Duper Program" file.ico "" checked
""")
assert xpackArgs.contains("-welcome-image")
assert xpackArgs.contains("-installer-image")
assert xpackArgs.contains("-uninstaller-image")
