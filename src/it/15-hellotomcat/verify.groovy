String ext = System.properties['os.name'].contains("Windows")?".exe":""

exeFile = new File( basedir, "target/jet/app/bin/HelloTomcat" + ext)
assert exeFile.exists()
File zipFile = new File(basedir, "target/jet/HelloTomcat-1.0-SNAPSHOT.zip")
assert zipFile.exists()

File commonsIo = new File(basedir, "target/jet/build/lib/commons-io-1.3.2.jar")
File prj = new File(basedir, "target/jet/build/HelloTomcat.prj")

commonsIo.exists()
prj.text.contains("""!classloaderentry webapp webapps/ROOT:/WEB-INF/lib/commons-io-1.3.2.jar
  -optimize=autodetect
  -protect=nomatter
  -pack=all
!end""")
