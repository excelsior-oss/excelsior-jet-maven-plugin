import com.excelsiorjet.TestUtils

assert new File(basedir, "target/jet/build/run.out").text.replace("/",File.separator).trim().equals(
        new File(basedir, "target/jet/build/rt").getAbsolutePath())

String ext = TestUtils.exeExt()
File workingDir = new File(basedir, "target/jet/app");
File exeFile = new File(workingDir, "JVMArgs" + ext)
assert exeFile.exists()

exeFile.getAbsolutePath().execute(null, workingDir).waitForOrKill(2000)

assert new File(basedir, "target/jet/app/run.out").text.replace("/", File.separator).trim().equals(
        new File(basedir, "target/jet/app/rt").getAbsolutePath())


