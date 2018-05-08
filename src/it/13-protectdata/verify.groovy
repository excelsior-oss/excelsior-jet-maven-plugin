import com.excelsiorjet.TestUtils

String ext = TestUtils.exeExt()
File exeFile = new File( basedir, "target/jet/build/HelloWorld" + ext);
assert exeFile.exists()

assert !exeFile.text.contains("<mainClass>")
assert new File(basedir, "target/jet/build/HelloWorld_jetpdb/cryptseed").exists()
