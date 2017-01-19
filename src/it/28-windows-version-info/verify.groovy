versionRes = new File(basedir, "target/jet/build/HelloWorld_jetpdb/version.rc");
assert versionRes.exists()
String versionResText = versionRes.text;
assert versionResText.contains("MyCompany") &&
       versionResText.contains("MyProduct") &&
       versionResText.contains("4.3.2.1") &&
       versionResText.contains("MyCopyright") &&
       versionResText.contains("My description")

