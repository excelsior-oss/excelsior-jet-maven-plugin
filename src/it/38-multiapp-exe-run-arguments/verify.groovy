import com.excelsiorjet.TestUtils

String ext = TestUtils.exeExt()
File exeFile = new File( basedir, "target/jet/build/HelloWorld" + ext);
assert exeFile.exists()
exeFile = new File( basedir, "target/jet/app/HelloWorld" + ext)
assert exeFile.exists()

String buildLog = new File(basedir, "build.log").text

assert !buildLog.contains("No arguments specified")
assert !buildLog.contains("The application has terminated with exit code: 3")
assert buildLog.contains("The application has terminated with exit code: 0")
assert buildLog.contains("0: arg1")
assert buildLog.contains("1: arg2.1, arg2.2")
