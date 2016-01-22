String ext = System.properties['os.name'].contains("Windows")?".exe":""

File exeFile = new File( basedir, "target/jet/app/HelloWorld" + ext)
assert exeFile.exists()

if(System.properties['os.name'].contains("Windows")) {
    File optRtFile = new File(basedir, "target/jet/app/rt/bin/jfxwebkit.dll");
    assert optRtFile.exists()
} else if(System.properties['os.name'].contains("OS X")) {
    File optRtFile = new File(basedir, "target/jet/app/rt/lib/libjfxwebkit.dylib");
    assert optRtFile.exists()
} else if(System.properties['os.name'].contains("Linux")) {
    if(new File(basedir, "target/jet/app/rt/lib/amd64").exists()) {
        File optRtFile = new File(basedir, "target/jet/app/rt/lib/amd64/libjfxwebkit.so");
        assert optRtFile.exists()
    } else if(new File(basedir, "target/jet/app/rt/lib/i386").exists()) {
        File optRtFile = new File(basedir, "target/jet/app/rt/lib/i386/libjfxwebkit.so");
        assert optRtFile.exists()
    } else {
        assert false
    }
} else {
    assert false
}

File optRtFile = new File( basedir, "target/jet/app/rt/lib/ext/nashorn.jar")
assert optRtFile.exists()