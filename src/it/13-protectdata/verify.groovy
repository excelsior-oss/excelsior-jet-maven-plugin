String ext = System.properties['os.name'].contains("Windows")?".exe":""
File exeFile = new File( basedir, "target/jet/build/HelloWorld" + ext);
assert exeFile.exists()

assert !exeFile.text.contains("<mainClass>")
