jetRtFilesCount = new File(basedir, "target/jet/app/rt/jetrt").listFiles().length;
File rt0Jar = new File(basedir, "target/jet/app/rt/lib/rt-0.jar")
assert jetRtFilesCount > 1 || rt0Jar.exists();



