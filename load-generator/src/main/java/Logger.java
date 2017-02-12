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

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author phd
 */
@Log4j2
public class Logger {
    private Path logPath;
    private volatile ConcurrentLinkedQueue<List<String>> queue;
    private volatile ExecutorService executorService;
    private volatile boolean isShutdowned = false;

    public Logger(Path logPath) throws IOException {
        this.logPath = logPath;
        queue = new ConcurrentLinkedQueue<>();
        logPath.getParent().toFile().mkdirs();
        FileWriter fileWriter = new FileWriter(logPath.toFile(), true);
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
                while (!isShutdowned || !queue.isEmpty()) {
                    List<String> logs = queue.poll();
                    if (logs != null) {
                        try {
                            for (String log: logs) {
                                fileWriter.write(log + '\n');
                            }
                            fileWriter.flush();
                        } catch (IOException e) {
                            log.error(e);
                        }
                    }
                }
            }
        );
    }

    public void addLogs(List<String> logs) {
        queue.add(logs);
    }

    public void shutdown() {
        isShutdowned = true;
        executorService.shutdown();
    }


}
