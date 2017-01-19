mvn release:prepare
mvn release:perform -P release -Darguments="-Dinvoker.test=ignore" >release.out