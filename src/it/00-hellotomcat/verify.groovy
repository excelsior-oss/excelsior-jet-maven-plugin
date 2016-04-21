String ext = System.properties['os.name'].contains("Windows")?".exe":""

exeFile = new File( basedir, "target/jet/app/bin/HelloTomcat" + ext)
assert exeFile.exists()
File zipFile = new File(basedir, "target/jet/HelloTomcat-1.0-SNAPSHOT.zip")
assert zipFile.exists()
