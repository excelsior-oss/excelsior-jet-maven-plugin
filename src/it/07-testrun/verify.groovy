import com.excelsiorjet.TestUtils

if (TestUtils.isStartupProfileGenerationSupported()) {
  File startupProfile = new File(basedir, "src/main/jetresources/HelloWorld.startup");
  assert startupProfile.exists()
  File reorderFile = new File(basedir, "target/jet/build/HelloWorld_jetpdb/reorder.li")
  assert reorderFile.exists();
}
assert new File( basedir, "target/jet/build/custom.file").exists()
assert new File( basedir, "target/jet/build/subdir/subdir.file").exists()
