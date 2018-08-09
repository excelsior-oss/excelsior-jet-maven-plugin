import com.excelsiorjet.TestUtils

String ext = TestUtils.exeExt()

exeFile = new File( basedir, "target/jet/app/spring-boot-sample-tomcat" + ext)
assert exeFile.exists()

//check jar is not packed into exe
jarFile = new File( basedir, "target/jet/app/spring-boot-sample-tomcat-1.0-SNAPSHOT.jar")
assert jarFile.exists()

File zipFile = new File(basedir, "target/jet/spring-boot-sample-tomcat-1.0-SNAPSHOT.zip")
assert zipFile.exists()

//check profiles
profileExeFile = new File( basedir, "target/jet/appToProfile/spring-boot-sample-tomcat" + ext)
assert profileExeFile.exists()
File profileFile = new File(basedir, "src/main/jetresources/spring-boot-sample-tomcat.jprof");
assert profileFile.exists()
File startupProfile = new File(basedir, "src/main/jetresources/spring-boot-sample-tomcat.startup");
assert startupProfile.exists()
File usgProfile = new File(basedir, "src/main/jetresources/spring-boot-sample-tomcat.usg");
assert usgProfile.exists()
assert usgProfile.text.contains("springboot%")

//replace line separators to Unix as Groovy """ multiline strings produce Unix line separators
File prj = new File(basedir, "target/jet/build/spring-boot-sample-tomcat.prj")
assert prj.exists()
def prjText = prj.text.replaceAll("\r\n", "\n")
assert prjText.contains("""!classloaderentry app spring-boot-sample-tomcat-1.0-SNAPSHOT.jar
  -pack=none
!end""")


assert prjText.contains("""!classloaderentry springboot spring-boot-sample-tomcat-1.0-SNAPSHOT.jar:/BOOT-INF/lib/spring-webmvc-5.0.8.RELEASE.jar
  -optimize=all
  -protect=nomatter
!end""")
