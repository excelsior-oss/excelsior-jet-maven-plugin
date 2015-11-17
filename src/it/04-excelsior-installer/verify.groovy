boolean isWindows = System.properties['os.name'].contains("Windows");
boolean isOX = System.properties['os.name'].contains("OS X");
String ext = isWindows?".exe":""
File exeFile = new File( basedir, "target/jet/build/HelloSwing" + ext);
assert exeFile.exists()
exeFile = new File( basedir, "target/jet/app/HelloSwing" + ext)
assert exeFile.exists()
installer = new File(basedir, "target/jet/HelloSwing-1.2.3-SNAPSHOT" + ext)
assert isOX || installer.exists();
versionRes = new File(basedir, "target/jet/build/jetpdb/version.rc");
assert !versionRes.exists() || versionRes.text.contains("1.2.3")
