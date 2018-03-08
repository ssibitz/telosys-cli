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
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Application entry point for CLI
 * 
 * @author Laurent GUERIN
 *
 */
public class LaunchArgumentsProcessor {
	
	private final PrintWriter out ;

	private final CommandProvider commandProvider ;
	
	/**
	 * Constructor
	 * @throws IOException
	 */
	public LaunchArgumentsProcessor(CommandProvider commandProvider, PrintWriter out) {
		this.commandProvider = commandProvider ;
		this.out = out ;
	}
	
	/**
	 * Processes the launch arguments if any <br>
	 * eg : -h homedir 
	 * @param args
	 */
	protected void processLaunchArguments(String[] args) {
		try {
			processArguments(args);
		} catch (Exception e) {
			print("ERROR : Unexpected exception " + e.getMessage() );
		}
	}
	
	private void print(String message) {
		out.println(message);
		out.flush();
	}

	/**
	 * Processes the arguments if any <br>
	 * eg : -h homedir 
	 *  
	 * @param args
	 */
	private void processArguments(String[] args) {
		if ( args.length > 0 ) {
			if ( Trace.DEBUG ) {
				print("");
				printArguments(args);
			}
		}
		// Is there a "-h homedir" argument ?
		File home = getHomeArg(args);
		if ( home != null ) {
			if ( Trace.DEBUG ) {
				print("Setting 'home' to '" + home.getAbsolutePath() + "'") ;
			}
			// Set current working directory : command "cd path"
			Command cdCommand = commandProvider.getCommand("cd");
			String[] cdArgs = buildArgs(cdCommand, home.getAbsolutePath());
			cdCommand.execute(cdArgs);
			// Set 'HOME' : command "h path"
			Command homeCommand = commandProvider.getCommand("h");
			String[] hArgs = buildArgs(cdCommand, home.getAbsolutePath());
			String r = homeCommand.execute( hArgs );
			print(r) ; // Prints the "h" command result : Home set ('xxx')
		}
		// Is there a "-ac autoconfirm" argument ?
		String autoconfirm = getStringArg("-ac", args);
		if ( autoconfirm != null ) {
			// Set current model
			Command autoconfirmCommand = commandProvider.getCommand("ac");
			print(autoconfirmCommand.execute( buildArgs(autoconfirmCommand, autoconfirm)));
		}
		// Is there a "-m model" argument ?
		String model = getStringArg("-m", args);
		if ( model != null ) {
			// Set current model
			Command modelCommand = commandProvider.getCommand("m");
			print(modelCommand.execute( buildArgs(modelCommand, model)));
		}
		// Is there a "-b bundle" argument ?
		String bundle = getStringArg("-b", args);
		if ( bundle != null ) {
			// Set current bundle
			Command bundleCommand = commandProvider.getCommand("b");
			print(bundleCommand.execute( buildArgs(bundleCommand, bundle)));
		}
		// Is there a "-udbm" argument ?
		String udbm = getStringArg("-udbm", args);
		if ( udbm != null) {
			// Set current model
			Command updateCommand = commandProvider.getCommand("udbm");
			print(updateCommand.execute( buildArgs(updateCommand, udbm)));
		}
		// Is there a "-ndbm" argument ?
		String ndbm = getStringArg("-ndbm", args);
		if ( ndbm != null) {
			// Set current model
			Command newCommand = commandProvider.getCommand("ndbm");
			print(newCommand.execute( buildArgs(newCommand, ndbm)));
		}
		// Is there a "-gen *" argument ?
		String gen = getStringArg("-gen", args);
		if ( gen != null ) {
			print("");
			print("Start generating models, services, interfaces from templates...'");
			print("");
			// Set current model
			Command genCommand = commandProvider.getCommand("gen");
			print(genCommand.execute( buildArgs(genCommand, "*", "*", "-r")));
			if (genCommand.hasCommandErrors()) {
				print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				print("  Generation has errors - please check ");
				print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			} else {
				// Quit generation if set:
				if (gen.trim().equalsIgnoreCase("+autoend")) {
					Command quitCommand = commandProvider.getCommand("q");
					print(quitCommand.execute( buildArgs(quitCommand)));
				}
			}
		}
	}

	private String[] buildArgs(Command command) {
		String[] args = new String[1];
		args[0] = command.getName();
		return args;
	}
	private String[] buildArgs(Command command, String commandArg1) {
		String[] args = new String[2];
		args[0] = command.getName();
		args[1] = commandArg1;
		return args;
	}
	private String[] buildArgs(Command command, String commandArg1, String commandArg2) {
		String[] args = new String[3];
		args[0] = command.getName();
		args[1] = commandArg1;
		args[2] = commandArg2;
		return args;
	}
	private String[] buildArgs(Command command, String commandArg1, String commandArg2, String commandArg3) {
		String[] args = new String[4];
		args[0] = command.getName();
		args[1] = commandArg1;
		args[2] = commandArg2;
		args[3] = commandArg3;
		return args;
	}

	private void printArguments(String[] args) {
		StringBuilder sb = new StringBuilder();
		sb.append("args : ");
		for (String s : args ) {
			sb.append(s);
			sb.append(" ");
		}
		print(sb.toString());
	}
	
	/**
	 * Returns a File pointing on the "-h" given folder (or null if none)
	 * @param args
	 * @return
	 */
	private File getHomeArg(String[] args) {
		// args do not containts the initial command ( eg "java -jar xxx" ) 
		// so the usefull arguments starts at ZERO 
		for ( int i = 0 ; i < args.length ; i++ ) {
			String arg = args[i];
			if ( "-h".equals(arg) ) {
				String home = getNextArg(args, i);
				if ( home != null ) {
					if (home.startsWith(".")) {
						Path basePath = FileSystems.getDefault().getPath(System.getProperty("user.dir"));
						Path resolvedPath = basePath.resolve(home);
						Path absolutePath = resolvedPath.normalize();
						home = absolutePath.toString();
					}
					File file = new File(home);
					if ( file.exists() && file.isDirectory() ) {
						// OK, home argument is valid
						return file ;
					}
					else {
						print("Invalid 'home' argument : '" + home + "'");
					}
				}
			}
		}
		return null ;
	}

	/**
	 * Returns a String pointing on the parameter (or null if none)
	 * @param paramName, args
	 * @return
	 */
	private String getStringArg(String paramName, String[] args) {
		// args do not containts the initial command ( eg "java -jar xxx" )
		// so the usefull arguments starts at ZERO
		for ( int i = 0 ; i < args.length ; i++ ) {
			String arg = args[i];
			if ( paramName.equals(arg) ) {
				String paramValue = getNextArg(args, i);
				if ( paramValue != null ) {
					return paramValue ;
				}
			}
		}
		return null ;
	}

	private String getNextArg(String[] args, int i) {
		if ( i+1 < args.length ) {
			return args[i+1] ;
		}
		else {
			return null ;
		}
	}

}
