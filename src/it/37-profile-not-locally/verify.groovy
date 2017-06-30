import com.excelsiorjet.TestUtils

assert new File( basedir, "target/jet/appToProfile.zip").exists()
File profileDir = new File( basedir, "target/jet/appToProfile")
profileExeFile = new File(profileDir, "Test" + TestUtils.exeExt())
assert profileExeFile.exists()

if (!TestUtils.crossCompilation) {
  assert profileExeFile.getAbsolutePath().execute(null, profileDir).text.contains("Time:")
  assert new File(profileDir, "Test.jprof").exists()
}
