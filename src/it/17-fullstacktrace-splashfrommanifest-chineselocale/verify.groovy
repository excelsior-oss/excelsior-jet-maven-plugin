boolean isWindows = System.properties['os.name'].contains("Windows");
String ext = isWindows?".exe":""
File exeFile = new File( basedir, "target/jet/build/HelloWorld" + ext);
assert exeFile.exists()
exeFile = new File( basedir, "target/jet/app/HelloWorld" + ext)
assert exeFile.exists()
String cmd = exeFile.getAbsolutePath();

//check lines in stack trace
assert (cmd.execute().text.trim().equals("Hello World"))

//check splash in resources
assert new File(basedir, "target/jet/build/HelloWorld_jetpdb/tmpres/splash.png").exists()

//check chinese locale
assert !new File(basedir, "target/jet/app/rt/jetrt").list().findAll{it.contains("XLCH")}.isEmpty()

//check no european locale
assert new File(basedir, "target/jet/app/rt/jetrt").list().findAll{it.contains("XLEU")}.isEmpty()

