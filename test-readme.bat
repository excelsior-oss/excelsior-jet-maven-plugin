@echo off
php -d error_log=error.log -f README.md.php maven > README-maven.md
if errorlevel 1 goto :eof
diff -w -B README-maven.md README.md 
php -d error_log=error.log -f README.md.php gradle > README-gradle.md
