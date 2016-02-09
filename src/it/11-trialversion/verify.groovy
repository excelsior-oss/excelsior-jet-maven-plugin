assert new File(basedir, "target/jet/build/HelloWorld_jetpdb/HelloWorld.rsp").text.contains("expire")

String ext = System.properties['os.name'].contains("Windows")?".exe":""
File exeFile = new File( basedir, "target/jet/app/HelloWorld" + ext)
assert exeFile.exists()

String cmd = exeFile.getAbsolutePath();

def proc = cmd.execute()
def b = new StringBuffer()
assert proc.consumeProcessErrorStream(b)
proc.waitForOrKill(2000)

assert (b.toString().trim().equals("App is expired"))

