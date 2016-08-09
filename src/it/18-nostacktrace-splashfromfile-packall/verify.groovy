boolean isWindows = System.properties['os.name'].contains("Windows");
String ext = isWindows?".exe":""
File exeFile = new File( basedir, "target/jet/build/HelloWorld" + ext);
assert exeFile.exists()
exeFile = new File( basedir, "target/jet/app/HelloWorld" + ext)
assert exeFile.exists()
String cmd = exeFile.getAbsolutePath();

//check java.lang.Void in stack trace
assert (cmd.execute().text.trim().equals("Hello World"))

//check splash in .rsp
assert new File(basedir, "target/jet/build/HelloWorld_jetpdb/HelloWorld.rsp").text.contains("splash.png")

//check -pack=all and splash from file (tmpres does not exist as no resources shoould be prepared)
assert !new File(basedir, "target/jet/build/HelloWorld_jetpdb/tmpres").exists()

