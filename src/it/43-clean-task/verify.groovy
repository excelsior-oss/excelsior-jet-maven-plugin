import com.excelsiorjet.TestUtils

String ext = TestUtils.exeExt();

File exeFile = new File( basedir, "target/jet/build/HelloWorld" + ext);
assert exeFile.exists()
assert !(new File(basedir, "target/jet/build/AppWithDep_jetpdb").exists())
