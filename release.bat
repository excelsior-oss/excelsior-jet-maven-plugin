call mvn release:prepare
call mvn release:perform -P release -Darguments="-Dinvoker.test=ignore" >release.out