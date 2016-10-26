import com.excelsiorjet.TestUtils

String ext = TestUtils.exeExt()

File exeFile = new File( basedir, "target/jet/app/HelloWorld" + ext)
assert exeFile.exists()

assert ["target/jet/app/rt/bin/jfxwebkit.dll",
        "target/jet/app/rt/lib/libjfxwebkit.dylib",
        "target/jet/app/rt/lib/amd64/libjfxwebkit.so",
        "target/jet/app/rt/lib/i386/libjfxwebkit.so",
        "target/jet/app/rt/lib/arm/libjfxwebkit.so"].any { new File(basedir, it).exists()}

File optRtFile = new File( basedir, "target/jet/app/rt/lib/ext/nashorn.jar")
assert optRtFile.exists()