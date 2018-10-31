import com.excelsiorjet.TestUtils

File testRunFailed = new File( basedir, "target/jet/build/failed");
assert !testRunFailed.exists()
File runFailed = new File( basedir, "target/jet/app/failed")
assert !runFailed.exists()
File profileFailed = new File( basedir, "target/jet/appToProfile/failed")
assert !profileFailed.exists()
