/******************************************************************************
 * Blockchain benchmarking framework                                          *
 * Copyright (C) 2017 DSX Technologies Limited.                               *
 * *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 * *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 * *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 * *
 * Removal or modification of this copyright notice is prohibited.            *
 * *
 ******************************************************************************/

import java.util.ArrayList;
import java.util.List;

/**
 * @author phd
 */
public class Main {
    public static void main(String[] args) throws Exception {
        int amountOfTransactions = Integer.parseInt(args[0]);
        int amountOfThreadsPerTarget = Integer.parseInt(args[1]);
        int minLength = Integer.parseInt(args[2]);
        int maxLength = Integer.parseInt(args[3]);
        int delay = Integer.parseInt(args[4]);
        List<String> targets = new ArrayList<>();
        for (int i = 4; i < args.length; ++i) {
            targets.add(args[i]);
        }
        LoadManager loadManager = new LoadManager(
                targets,
                amountOfTransactions,
                amountOfThreadsPerTarget,
                minLength,
                maxLength,
                delay
        );
        loadManager.start();
        loadManager.waitCompletion();
    }
}
