import com.excelsiorjet.TestUtils

assert new File(basedir, "target/jet/build/HelloWorld_jetpdb/HelloWorld.rsp").text.contains("expire")

String ext = TestUtils.exeExt()
File exeFile = new File( basedir, "target/jet/app/HelloWorld" + ext)
assert exeFile.exists()

if (!TestUtils.crossCompilation) {
  String cmd = exeFile.getAbsolutePath();

  def proc = cmd.execute()
  def b = new StringBuffer()
  assert proc.consumeProcessErrorStream(b)
  proc.waitForOrKill(2000)

  assert (b.toString().trim().equals("App is expired"))
}

