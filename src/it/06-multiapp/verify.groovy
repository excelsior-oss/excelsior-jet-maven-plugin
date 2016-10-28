import com.excelsiorjet.TestUtils

String ext = TestUtils.exeExt()

File exeFile = new File( basedir, "target/jet/app/HelloWorld" + ext)
assert exeFile.exists()

if (!TestUtils.crossCompilation) {
  String cmd = exeFile.getAbsolutePath();

  assert (cmd.execute().text.trim().equals("Hello World"))
  assert ((cmd + " HelloWorld2").execute().text.trim().equals("Hello World2"))
}
