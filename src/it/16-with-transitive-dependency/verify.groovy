File dep = new File(basedir, "target/jet/build/AppWithDep_jetpdb/tmpres/jackson-databind-2.8.0__1.jar")
File transDep = new File(basedir, "target/jet/build/AppWithDep_jetpdb/tmpres/jackson-core-2.8.0__3.jar")
assert dep.exists()
assert transDep.exists()
