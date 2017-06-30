import com.excelsiorjet.TestUtils

String ext = TestUtils.exeExt();

File exeFile = new File( basedir, "target/jet/build/Test" + ext);
assert exeFile.exists()
assert exeFile.text.contains("<mainClass>")
profileExeFile = new File( basedir, "target/jet/appToProfile/Test" + ext)
assert profileExeFile.exists()
File profileFile = new File(basedir, "src/main/jetresources/Test.jprof");
assert profileFile.exists()
exeFile = new File( basedir, "target/jet/app/Test" + ext)
assert exeFile.exists()
File zipFile = new File(basedir, "target/jet/Test-1.0-SNAPSHOT.zip")
assert zipFile.exists()
