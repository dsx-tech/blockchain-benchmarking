/*
 * *****************************************************************************
 *  * Blockchain benchmarking framework                                          *
 *  * Copyright (C) 2016 DSX Technologies Limited.                               *
 *  * *
 *  * This program is free software: you can redistribute it and/or modify       *
 *  * it under the terms of the GNU General Public License as published by       *
 *  * the Free Software Foundation, either version 3 of the License, or          *
 *  * (at your option) any later version.                                        *
 *  * *
 *  * This program is distributed in the hope that it will be useful,            *
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 *  * See the GNU General Public License for more details.                       *
 *  * *
 *  * You should have received a copy of the GNU General Public License          *
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 *  * *
 *  * Removal or modification of this copyright notice is prohibited.            *
 *  * *
 *  *****************************************************************************
 */

package uk.dsxt.utils;

import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Log4j2
public class PrintOutputToConsole extends Thread {
    private InputStream is = null;

    private PrintOutputToConsole(InputStream is, String type) {
        this.is = is;
    }

    public void run() {
        String s;
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(is));
            while ((s = br.readLine()) != null) {
                System.out.println(s);
            }
        }
        catch (IOException ioe) {
            log.error("Cannot start PrintOutput class", ioe);
        }
    }


    public static PrintOutputToConsole getStreamWrapper(InputStream is, String type) {
        return new PrintOutputToConsole(is, type);
    }
}
