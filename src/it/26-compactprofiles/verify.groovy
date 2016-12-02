import com.excelsiorjet.TestUtils

String ext = TestUtils.exeExt();

assert new File(basedir, "target/jet/app/HelloWorld" + ext).exists()

assert new File(basedir, "target/jet/app/rt/lib/management").exists()

assert !new File(basedir, "target/jet/app/rt/lib/fonts").exists()
