@echo off
REM Load Environment vars
cls

title "Installing Library to Maven"
echo.
echo Installing Library to Maven

call mvn install:install-file -Dfile=telosys-tools-all-3.0.0.jar -DgroupId=org.telosys -DartifactId=telosys-tools-all -Dversion=3.0.0 -Dpackaging=jar -DgeneratePom=true

echo.
echo Done !

pause