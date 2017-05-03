#!/bin/bash
export M2_HOME=/home/user/apache-maven-3.3.3
export M2=$M2_HOME/bin
export PATH=$M2:$PATH
export JAVA_HOME=/usr/java/jdk1.8.0_25
home=/home/user
tests[0]="${home}/jet11.0-std-x86;notomcat"
tests[1]="${home}/jet11.0-pro-x86;notomcat"
tests[2]="${home}/jet11.0-ent-x86;${home}/apache-tomcat-6.0.18"
tests[3]="${home}/jet11.0-pro-amd64;notomcat"
tests[4]="${home}/jet11.0-ent-amd64;${home}/apache-tomcat-7.0.62"
tests[5]="${home}/jet11.3-ent-amd64;${home}/apache-tomcat-8.0.26"
tests[6]="${home}/jet11.3-pro-amd64;notomcat"
tests[7]="${home}/jet11.3-ent-x86;${home}/apache-tomcat-6.0.18"
tests[8]="${home}/jet11.3-pro-x86;notomcat"
tests[9]="${home}/jet11.3-std-x86;notomcat"
tests[10]="${home}/jet11.3-eval-amd64;${home}/apache-tomcat-8.0.26"

for t in ${tests[*]};
do
  test=(${t//;/ })
  echo Testing Excelsior JET - ${test[0]} with Tomcat - ${test[1]}
  mvn clean install  -Djet.home=${test[0]} -Dtomcat.home=${test[1]}
done 
