import com.excelsiorjet.TestUtils

File dep = new File(basedir, "target/jet/build/AppWithDep_jetpdb/tmpres/commons-io-1.3.2__1.jar")
assert dep.exists()

File prj = new File(basedir, "target/jet/build/AppWithDep.prj")
String prjText = TestUtils.toUnixLineSeparators(prj.text);
assert prjText.contains("""!classpathentry lib/commons-io-1.3.2.jar
  -optimize=all
  -protect=all
!end""")

