/**
 *  Copyright (C) 2015-2017  Telosys project org. ( http://www.telosys.org/ )
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.gnu.org/licenses/lgpl.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.telosys.tools.cli;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jline.console.ConsoleReader;

import org.telosys.tools.api.TelosysProject;
import org.telosys.tools.commons.FileUtil;
import org.telosys.tools.commons.StrUtil;
import org.telosys.tools.commons.TelosysToolsException;
import org.telosys.tools.commons.bundles.TargetsDefinitions;

/**
 * Command abstract class
 * 
 * @author Laurent GUERIN
 *
 */
public abstract class Command {

	private final ConsoleReader consoleReader ;
	private final PrintWriter out ;
	private final Environment environment ;
	
	/**
	 * Constructor
	 * @param consoleReader
	 * @param environment
	 */
	public Command(ConsoleReader consoleReader, Environment environment ) {
		super();
		this.consoleReader = consoleReader ;
		this.out = new PrintWriter(consoleReader.getOutput()) ;
		this.environment = environment ;
	}
	
	//----------------------------------------------------------------
	/**
	 * Returns the command name 
	 * @return
	 */
	public abstract String getName();
	
	/**
	 * Returns a short description
	 * @return
	 */
	public abstract String getShortDescription();
	
	/**
	 * Returns the command description
	 * @return
	 */
	public abstract String getDescription();
	
	/**
	 * Returns the usage 
	 * @return
	 */
	public abstract String getUsage();
	
	/**
	 * Executes the command with the given arguments
	 * @param args
	 * @return
	 */
	public abstract String execute(String[] args);
	
	//----------------------------------------------------------------
	protected Environment getEnvironment() {
		return environment ;
	}

	protected void appendLine(StringBuilder sb, String s) {
		sb.append(s);
		sb.append(Environment.LINE_SEPARATOR);
	}
	
	protected void appendEndOfLine(StringBuilder sb) {
		sb.append(Environment.LINE_SEPARATOR);
	}

	protected String invalidUsage(String message) {
		return "Invalid usage : " + message ;
	}

	protected int readChar() {
		try {
			return consoleReader.readCharacter();
		} catch (IOException e) {
			printError(e);
			throw new CancelCommandException("IOException : readCharacter()");
		}
	}
	protected void backspace() {
		try {
			consoleReader.backspace();
		} catch (IOException e) {
			printError(e);
			throw new CancelCommandException("IOException : backspace()");
		}
	}
	
	protected String readResponse() {
		StringBuilder sb = new StringBuilder();
		while ( true ) {
			char c = (char) readChar();
			if ( c >= ' ' ) {
				sb.append(c);
				out.print(c);
				out.flush();
			}
			else {
				switch(c) {
				case '\n' :
				case '\r' :
					out.println("");
					out.flush();
					return sb.toString();
				case '\b' :
					backspace();
					if ( sb.length() > 0 ) {
						sb.setLength(sb.length() - 1); // back (remove last char)
						out.print('\b');
						out.print(' ');
						out.print('\b');
						out.flush();
					}
					break ;
				}
			}			
		}
	}
	
	protected boolean confirm(String message) {
		out.print( message + " [y/n] ? " );
		out.flush();
		// If autoconfirm enabled do not ask user:
		String r;
		if (getCurrentAutoConfirm()) {
			r = "Y";
			out.println("Automatically confirmed because autoconfirm is set to on");
			out.flush();
		} else {
			r = readResponse() ;
		}
		return "Y".equalsIgnoreCase(r) ;
	}

	protected void print(String message) {
		out.println(message);
		out.flush();
	}
	protected void printError(String message) {
		out.println("[ERROR] : " + message );
		out.flush();
	}
	protected void printError(Exception ex) {
		LastError.setError(ex);
		out.println("[ERROR] Exception class   : " + ex.getClass().getSimpleName() );
		out.println("[ERROR] Exception message : " + ex.getMessage() );
		out.flush();
	}
	protected void printDebug(String message) {
		if ( Trace.DEBUG ) {
			out.println("[DEBUG] "+message);
			out.flush();
		}
	}

	/**
	 * Prints the given list of strings with a ' . ' at the beginning 
	 * @param strings
	 * @return
	 */
	protected void printList(List<String> strings) {
		if ( strings != null ) {
			for ( String s : strings ) {
				out.println(" . " + s);
			}
		}
		out.flush();
	}	

	protected String getCurrentDirectory() {
		return environment.getCurrentDirectory();
	}

	//-------------------------------------------------------------------------
	// Home directory 
	//-------------------------------------------------------------------------
	protected boolean checkDirectory(String dir) {
		if (dir == null) {
			print("Directory is null !");
			return false;
		}
		return checkDirectory(new File(dir) );
	}
	
	protected boolean checkDirectory(File file) {
		if (file == null) {
			print("Directory is null !");
			return false;
		}
		if ( ! file.exists() ) {
			print("'" + file.getAbsolutePath() + "' doesn't exist !");
			return false;
		}
		if ( ! file.isDirectory()) {
			print( "'" + file.getAbsolutePath() + "' is not a directory !" );
			return false;
		} 
		return true ; // OK
	}
	
	//-------------------------------------------------------------------------
	// Home directory 
	//-------------------------------------------------------------------------
	protected boolean checkHomeDirectoryDefined() {
		if ( environment.getHomeDirectory() != null ) {
			return true ;
		}
		else {
			print( "Home directory must be set before using this command!" ) ;
			return false ;
		}
	}	

	protected void setCurrentHome() {
		environment.setHomeDirectory();
		updatePrompt();
	}
	
	/**
	 * @param directory
	 */
	protected void setCurrentHome(String directory) {
		environment.setHomeDirectory(directory);
		updatePrompt();
	}
	
	/**
	 * Returns the current AutoConfirm
	 * @return
	 */
	protected Boolean getCurrentAutoConfirm() {
		return environment.getAutoconfirm();
	}

	/**
	 * @param autoconfirm
	 */
	protected void setCurrentAutoConfirm(Boolean autoconfirm) {
		environment.setAutoconfirm(autoconfirm);
		updatePrompt();
	}

	/**
	 * Returns the current home directory
	 * @return
	 */
	protected String getCurrentHome() {
		return environment.getHomeDirectory();
	}

	//-------------------------------------------------------------------------
	/**
	 * Check the number of arguments
	 * @param args all the arguments retrieved from the command line
	 * @param n list of acceptable number of arguments without the command itself (without args[0])
	 * @return
	 */
	protected boolean checkArguments(String[] args, int ... n) {
		boolean ok = false ;
		int argsCount = args.length - 1 ;
		for ( int i : n ) {
			if ( argsCount == i ) {
				ok = true ;
			}
		}
		if ( ! ok ) {
			print("Invalid usage : unexpected number of arguments");
		}
		return ok ;
	}

	//-------------------------------------------------------------------------
	// Model
	//-------------------------------------------------------------------------
	/**
	 * Checks if a model is defined
	 * @return
	 */
	protected boolean checkModelDefined() {
		if ( environment.getCurrentModel() != null ) {
			return true ;
		}
		else {
			print( "This command requires a model." ) ;
			return false ;
		}
	}	

	/**
	 * Checks if a model is defined and is a DSL model type
	 * @return
	 */
	protected boolean checkDslModelDefined() {
		if ( checkModelDefined() ) {
			if ( isDslModel( getCurrentModel() ) ) {
				return true ;
			}
			else {
				print("This command requires a DSL model.");
			}
		}
		return false ;
	}	

	/**
	 * Set the current model in the current environment
	 * @param modelName
	 */
	protected void setCurrentModel(File modelFile) {
		if ( modelFile.exists() ) {
			setCurrentModel(modelFile.getName());
		}
		else {
			printError("Model file '" + modelFile.getName() + "' does not exist");
		}
	}
	
	protected boolean isCurrentModel(File modelFile) {
		if ( modelFile != null ) {
			return modelFile.getName().equals(environment.getCurrentModel() ) ;
		}
		return false ;
	}
	
	/**
	 * Set the current model name in the current environment
	 * @param modelName
	 */
	protected void setCurrentModel(String modelName) {
		environment.setCurrentModel(modelName);
		updatePrompt();
	}
	
	/**
	 * Unset the current model in the current environment
	 */
	protected void unsetCurrentModel() {
		environment.setCurrentModel(null);
		updatePrompt();
	}
	
	/**
	 * Returns the current model in the environment
	 * @return the current model (or null if none)
	 */
	protected String getCurrentModel() {
		return environment.getCurrentModel();
	}
	
	/**
	 * Returns true if the given model name is a DSL model (ends with ".model")
	 * @param modelName
	 * @return
	 */
	protected boolean isDslModel(String modelName) {
		if ( modelName != null ) {
			return modelName.endsWith(".model");
		}
		return false ;
	}
	//-------------------------------------------------------------------------
	// Bundle
	//-------------------------------------------------------------------------
	protected boolean checkBundleDefined() {
		if ( environment.getCurrentBundle() != null ) {
			return true ;
		}
		else {
			print( "Bundle-name must be set before using this command!" ) ;
			return false ;
		}
	}	

	/**
	 * Set the current bundle name in the current environment
	 * @param bundleName
	 */
	protected void setCurrentBundle(String bundleName) {
		environment.setCurrentBundle(bundleName);
		updatePrompt();
	}
	
	/**
	 * Returns the current bundle in the environment
	 */
	protected String getCurrentBundle() {
		return environment.getCurrentBundle();
	}

	/**
	 * Returns the TargetsDefinitions for the current bundle
	 * @return
	 */
	protected TargetsDefinitions getCurrentTargetsDefinitions() {
		TelosysProject telosysProject = getTelosysProject();
		try {
			return telosysProject.getTargetDefinitions( getCurrentBundle() );
		} catch (TelosysToolsException e) {
			printError(e);
			throw new CancelCommandException("Cannot get TargetsDefinitions");
		}
	}
	
	//-------------------------------------------------------------------------
	// GitHub store
	//-------------------------------------------------------------------------
	protected boolean checkGitHubStoreDefined() {
		if ( environment.getCurrentGitHubStore() != null ) {
			return true ;
		}
		else {
			print( "GitHub store must be set before using this command!" ) ;
			return false ;
		}
	}	
	
	protected void setCurrentGitHubStore(String storeName) {
		environment.setCurrentGitHubStore(storeName);
	}

	protected String getCurrentGitHubStore() {
		return environment.getCurrentGitHubStore();
	}
	
	protected String getDefaultGitHubStore() {
		return environment.getDefaultGitHubStore();
	}
	
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	
	protected void updatePrompt() {
		String prompt = Const.PROMPT_TEXT ;
		if ( environment.getHomeDirectory() != null ) {
			prompt = prompt + "#" ;
		}
		if ( environment.getCurrentModel() != null ) {
			prompt = prompt + "(" + environment.getCurrentModel() + ")" ;
		}
		if ( environment.getCurrentBundle() != null ) {
			prompt = prompt + "[" + environment.getCurrentBundle() + "]" ;
		}
		prompt = prompt + Const.PROMPT_CHAR ;
		consoleReader.setPrompt(Color.colorize(prompt, Const.PROMPT_COLOR));
	}
	
	/**
	 * Launches the external editor with the given file as argument 
	 * @param fileFullPath
	 * @return the full command line launched
	 */
	protected String launchEditor(String fileFullPath) {
		
		// Check the editor command is defined
		String editorCommand = environment.getEditorCommand();
		if ( editorCommand == null ) { 
			return "No editor command. Check the configuration.";
		}
		
		// Is there a file to be edited ?
		String fileToEdit = fileFullPath ;
		if ( StrUtil.nullOrVoid(fileToEdit) ) {
			fileToEdit = "" ; // for replacement in "xxx $FILE"
		}

		// Replace $FILE or ${FILE} if present
		String fullCommand ;
		if ( editorCommand.contains("$FILE") ) {
			fullCommand = StrUtil.replaceVar(editorCommand, "$FILE", fileToEdit);
		}
		else if ( editorCommand.contains("${FILE}") ) {
			fullCommand = StrUtil.replaceVar(editorCommand, "${FILE}", fileToEdit);
		}
		else {
			fullCommand = editorCommand + " " + fileToEdit;
		}
		// At this step the full comman is like "notepad foo.txt" 
		// or "/bin/sh /xx/xx/telosys-term.sh vi foo.txt"
		// or "specific-command foo.txt"

		// Run the OS command 
		SystemCommand.run(fullCommand, environment.getOperatingSystemType());
		return fullCommand ;
	}
	
	protected void launchSystemCommand(String fullCommand) {
		print("Launching command : " + fullCommand );
		try {
			Runtime.getRuntime().exec(fullCommand);
		} catch (IOException e) {
			printError(e);
		}
	}

	/**
	 * Returns the current Telosys project, or null if HOME is not defined
	 * @param environment
	 * @return
	 */
	protected TelosysProject getTelosysProject() {
		if ( checkHomeDirectoryDefined() ) {
			String projectFullPath = environment.getHomeDirectory();
			return new TelosysProject(projectFullPath);
		}
		return null ;
	}
	
	/**
	 * Returns the file full path for 'telosys-tools.cfg' if the file exists, else returns null
	 * @return
	 */
	protected String getTelosysToolsCfgFullPath() {
		return getFileFullPathIfExists(Const.TELOSYS_TOOLS_CFG, Const.TELOSYS_TOOLS_FOLDER);
	}

	/**
	 * Returns the file full path for 'databases.dbcfg' if the file exists, else returns null
	 * @return
	 */
	protected String getTelosysDbCfgFullPath() {
		return getFileFullPathIfExists(Const.DATABASES_DBCFG, Const.TELOSYS_TOOLS_FOLDER);
	}
	
	/**
	 * Returns the file full path or null is the file doesn't exist
	 * @param fileName
	 * @param subDirectory
	 * @return
	 */
	private String getFileFullPathIfExists(String fileName, String subDirectory) {
		File file = getFileInHomeDir(fileName, subDirectory);
		if (file.exists()) {
			return file.getAbsolutePath();
		}
		// Not found
		return null;
	}

	private File getFileInHomeDir(String fileName, String subDirectory) {

		// Try to get the file in the 'home' directory
		String shortPath = fileName; // e.g. 'telosys-tools.cfg' or 'databases.dbcfg'
		if (subDirectory != null) {
			shortPath = FileUtil.buildFilePath(subDirectory, fileName); 
			// e.g. 'TelosysTools/databases.dbcfg'
		}
		String fileFullPath = FileUtil.buildFilePath(environment.getHomeDirectory(), shortPath);
		return new File(fileFullPath);
	}

	/**
	 * Returns the string as is if not null, or "(undefined)" if null 
	 * @param s
	 * @return
	 */
	protected String undefinedIfNull(String s) {
		if ( s != null ) {
			return s ;
		}
		else {
			return "(undefined)";
		}
	}

	/**
	 * Returns if the generation succesed without any errors
	 */
	public Boolean hasCommandErrors() {
		return false;
	}

}   
