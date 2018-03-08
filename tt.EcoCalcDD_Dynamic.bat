@echo off

set CURR_PATH=%cd%
set DB=EcoCalcDD_Dynamic

title "Telosys Command Line Tools - Database Generator"

REM Call update database model, generate all templates
cd .\target\
java -jar telosys-cli-3.0.0-011.jar -h .\..\model\ -ac on -m %DB%.dbrep -b %DB% -udbm 1 -gen 

pause 