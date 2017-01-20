import com.excelsiorjet.TestUtils

File installerFile = new File(basedir, "target/jet/HelloTomcat-1.0-SNAPSHOT" + TestUtils.exeExt())
assert installerFile.exists()

String xpackArgs = new File(basedir, "target/jet/build/HelloTomcat.EI.xpack").text
assert xpackArgs.contains("-allow-user-to-change-tomcat-port")
