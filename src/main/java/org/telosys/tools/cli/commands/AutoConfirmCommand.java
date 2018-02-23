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

package org.telosys.tools.cli.commands;

import jline.console.ConsoleReader;
import org.telosys.tools.cli.Command;
import org.telosys.tools.cli.Environment;

public class AutoConfirmCommand extends Command {
    /**
     * Constructor
     * @param consoleReader
     * @param environment
     */
    public AutoConfirmCommand(ConsoleReader consoleReader, Environment environment) {
        super(consoleReader, environment);
    }

    @Override
    public String getName() {
        return "ac";
    }

    @Override
    public String getShortDescription() {
        return "Auto confirm" ;
    }

    @Override
    public String getDescription() {
        return "Print or set the 'Autoconfirm'";
    }

    @Override
    public String getUsage() {
        return "ac [on|off]";
    }

    @Override
    public String execute(String[] args) {
        if ( args.length > 1 ) {
            return tryToSetAutoConfirm(args[1]);
        }
        else {
            return undefinedIfNull(getCurrentAutoConfirm()?"on":"off");
        }
    }

    private String tryToSetAutoConfirm(String autoConfirm) {
        autoConfirm = autoConfirm.trim().toLowerCase();
        if ("on".equals(autoConfirm) ) {
            setCurrentAutoConfirm(true);
            return "Autoconfirm set to ('" + (getCurrentAutoConfirm()?"on":"off") + "')" ;
        }
        else if ( "off".equals(autoConfirm) ) {
            setCurrentAutoConfirm(false);
            return "Autoconfirm set to ('" + (getCurrentAutoConfirm()?"on":"off") + "')" ;
        }
        else {
            return "Invalid Autoconfirm parameter (<> on or off) '" + autoConfirm + "'";
        }
    }
}
