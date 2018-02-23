
TELOSYS-CLI ( README.TXT )

Requirements : 
 Java 7 (or +) must be installed 

Installation : 
 Unzip 'telosys-cli-x.x.x-xxx.zip' in a folder that will be added to the PATH
 Set the PATH in order to allow access from everywhere

Configuration :
 Edit 'telosys-cli.cfg' to set your prefered editor

Run :
 Use 'tt.bat' or 'tt.sh' to launch telosys-cli
 
 or completly run it from command line by using parameters:
 e.g.:
	-h .\..\model\ -ac on -m MyDatabase.dbrep -b MyDatabase -udbm * -gen +autoend 
 
	-h 	: Set Home-Path (Absolute or Relative)
	-ac : Set AutoConfirm to on|off 
	-m : Set Model to use
	-b : Set Bundle to use
	-udbm : Update selected database model
	-gen +autoend : Start generation with automatically ending if all ok.
	
