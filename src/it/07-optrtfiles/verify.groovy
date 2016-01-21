String ext = System.properties['os.name'].contains("Windows")?".exe":""

File exeFile = new File( basedir, "target/jet/build/HelloWorld" + ext);
assert exeFile.exists()

File optRtFile = new File( basedir, "target/jet/app/rt/bin/jfxwebkit.dll");
assert optRtFile.exists()
