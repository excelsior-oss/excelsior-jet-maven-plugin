String ext = System.properties['os.name'].contains("Windows")?".exe":""

File exeFile = new File( basedir, "target/jet/app/HelloWorld" + ext)
assert exeFile.exists()

String cmd = exeFile.getAbsolutePath();

assert (cmd.execute().text.trim().equals("Hello World"))
assert ((cmd + " HelloWorld2").execute().text.trim().equals("Hello World2"))
