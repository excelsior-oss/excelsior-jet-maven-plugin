assert new File(basedir, "target/jet/build/HelloWorld_jetpdb/HelloWorld.rsp").text.contains("RuntimeKind:CLASSIC")
assert new File( basedir, "target/jet/app/hidden/runtime").exists()
