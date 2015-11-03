boolean isWindows = System.properties['os.name'].contains("Windows");
String ext = isWindows?".exe":""
File exeFile = new File( basedir, "target/jet/build/HelloSwing" + ext);
assert exeFile.exists()
exeFile = new File( basedir, "target/jet/app/HelloSwing" + ext)
assert exeFile.exists()
assert !isWindows || new File(basedir, "target/jet/build/jetpdb/HelloSwing.rsp").text.contains("icon.ico")
assert !isWindows || new File(basedir, "target/jet/build/jetpdb/HelloSwing.rsp").text.contains("-sys=W")
