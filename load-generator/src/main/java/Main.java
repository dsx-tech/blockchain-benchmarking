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

import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.List;

/**
 * @author phd
 */
@Log4j2
public class Main {
    public static void main(String[] args) throws Exception {
        List<String> targets = Arrays.asList();
        LoadManager loadManager = new LoadManager(targets,
                50,
                10,
                20,
                40);
        loadManager.start();
        loadManager.stop();
    }
}
