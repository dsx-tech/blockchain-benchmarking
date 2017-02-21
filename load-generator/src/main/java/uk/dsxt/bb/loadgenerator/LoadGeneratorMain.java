/*
 ******************************************************************************
 * Blockchain benchmarking framework                                          *
 * Copyright (C) 2016 DSX Technologies Limited.                               *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************
 */

package uk.dsxt.bb.loadgenerator;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author phd
 */
@Log4j2
public class LoadGeneratorMain {
    public static void main(String[] args) {
        try {
            if (args.length != 5) {
                log.error("Incorrect arguments count in LoadGeneratorMain.main(). Need 5. Actual args: {}", Arrays.toString(args));
                return;
            }
            int amountOfTransactions = Integer.parseInt(args[0]);
            int amountOfThreadsPerTarget = Integer.parseInt(args[1]);
            int minLength = Integer.parseInt(args[2]);
            int maxLength = Integer.parseInt(args[3]);
            int delay = Integer.parseInt(args[4]);
            List<String> targets = new ArrayList<>();
            targets.addAll(Arrays.asList(args).subList(4, args.length));

            LoadManager loadManager = new LoadManager(targets, amountOfTransactions, amountOfThreadsPerTarget, minLength, maxLength, delay);

            loadManager.start();
            loadManager.waitCompletion();
        } catch (IOException e) {
            log.error("Couldn't start Load Generator module.");
        }
    }
}