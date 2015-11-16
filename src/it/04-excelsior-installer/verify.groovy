boolean isWindows = System.properties['os.name'].contains("Windows");
boolean isOX = System.properties['os.name'].contains("OS X");
String ext = isWindows?".exe":""
File exeFile = new File( basedir, "target/jet/build/HelloSwing" + ext);
assert exeFile.exists()
exeFile = new File( basedir, "target/jet/app/HelloSwing" + ext)
assert exeFile.exists()
installer = new File(basedir, "target/jet/HelloSwing-1.2.3-SNAPSHOT" + ext)
assert isOX || installer.exists();
assert !isWindows || new File(basedir, "target/jet/build/jetpdb/version.rc").text.contains("1.2.3")
