import com.excelsiorjet.TestUtils

String dllname = TestUtils.mangleDllName("HelloDll");

File dllFile = new File( basedir, "target/jet/build/" + dllname);
assert dllFile.exists()
assert !dllFile.text.contains("<mainClass>")
dllFile = new File( basedir, "target/jet/app/" + dllname)
assert dllFile.exists()
