import com.excelsiorjet.TestUtils

String ext = TestUtils.exeExt();

File exeFile = new File(basedir, "target/jet/build/SampleService" + ext);
assert exeFile.exists()
assert exeFile.text.contains("<mainClass>")
exeFile = new File(basedir, "target/jet/app/SampleService" + ext)
assert exeFile.exists()

File rspFile = new File(basedir, "target/jet/app/SampleService.rsp");
assert rspFile.exists();
String rspContents = TestUtils.toUnixLineSeparators(rspFile.text);
assert rspContents.equals(
"""-install SampleService.exe
-displayname "Sample Service"
-description "Sample Service created with Excelsior JET"
-auto
-dependence Dhcp
-dependence Dnscache
-args
arg
arg with space
"""
)

assert new File(basedir, "target/jet/app/install.bat").exists()
assert new File(basedir, "target/jet/app/uninstall.bat").exists()
File zipFile = new File(basedir, "target/jet/SampleService-1.0-SNAPSHOT.zip")
assert zipFile.exists()
