import com.excelsiorjet.TestUtils

String ext = TestUtils.exeExt();

File exeFile = new File( basedir, "target/jet/build/SampleService" + ext);
assert exeFile.exists()
assert exeFile.text.contains("<mainClass>")
exeFile = new File( basedir, "target/jet/app/SampleService" + ext)
assert exeFile.exists()
