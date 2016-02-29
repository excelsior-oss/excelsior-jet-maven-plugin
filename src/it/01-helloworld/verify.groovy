String ext = System.properties['os.name'].contains("Windows")?".exe":""

File exeFile = new File( basedir, "target/jet/build/HelloWorld" + ext);
assert exeFile.exists()
assert exeFile.text.contains("<mainClass>")
exeFile = new File( basedir, "target/jet/app/HelloWorld" + ext)
assert exeFile.exists()
File zipFile = new File(basedir, "target/jet/HelloWorld-1.0-SNAPSHOT.zip")
assert zipFile.exists()
