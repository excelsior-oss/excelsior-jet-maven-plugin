boolean isWindows = System.properties['os.name'].contains("Windows");
String ext = isWindows?".exe":""
File exeFile = new File( basedir, "target/jet/build/HelloWorld" + ext);
assert exeFile.exists()
exeFile = new File( basedir, "target/jet/app/HelloWorld" + ext)
assert exeFile.exists()

String buildLog = new File(basedir, "build.log").text

!buildLog.contains("No arguments specified")
!buildLog.contains("The application has terminated with exit code: 3")
buildLog.contains("The application has terminated with exit code: 0")
