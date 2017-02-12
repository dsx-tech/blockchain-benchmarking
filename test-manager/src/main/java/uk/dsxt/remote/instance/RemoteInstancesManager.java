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

package uk.dsxt.remote.instance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

/**
 * @author phd
 */
public class RemoteInstancesManager<T extends RemoteInstance> {
    private final static Logger logger = LogManager.getLogger(RemoteInstancesManager.class);

    private T rootInstance;
    private final List<T> commonInstances;
    private final List<T> instances;
    private final ExecutorService executorService;
    private final int THREADS_AMOUNT = 4;


    public RemoteInstancesManager() {
        commonInstances = new ArrayList<>();
        instances = new ArrayList<>();
        executorService = Executors.newFixedThreadPool(THREADS_AMOUNT);
    }

    public void setRootInstance(T remoteInstance) {
        instances.remove(rootInstance);
        rootInstance = remoteInstance;
        instances.add(rootInstance);
    }

    public T getRootInstance() {
        return rootInstance;
    }

    public void addCommonInstance(T remoteInstance) {
        commonInstances.add(remoteInstance);
        instances.add(remoteInstance);
    }

    public void addCommonInstances(List<T> remoteInstances) {
        commonInstances.addAll(remoteInstances);
        instances.addAll(remoteInstances);
    }

    public void executeCommandsForAll(List<String> commands) {
        executeCommands(instances, commands);
    }

    public void executeCommandsForCommon(List<String> commands) {
        executeCommands(commonInstances, commands);
    }

    public void executeCommandsForRoot(List<String> commands) {
        executeCommands(singletonList(rootInstance), commands);
    }

    public void uploadFilesForAll(List<Path> files) {
        uploadFiles(instances, files);
    }

    public void uploadFilesForCommon(List<Path> files) {
        uploadFiles(commonInstances, files);
    }

    public void uploadFilesForRoot(List<Path> files) {
        uploadFiles(singletonList(rootInstance), files);
    }

    private void executeCommands(List<T> remoteInstances, List<String> commands) {
        try {
            List<Callable<Boolean>> tasks = remoteInstances.stream()
                    .map(instance -> (Callable<Boolean>) () -> instance.sendCommands(resolveCommands(instance, commands)))
                    .collect(Collectors.toList());
            List<Future<Boolean>> futures = executorService.invokeAll(tasks);
            boolean succsess = futures.stream().map(f -> {
                try {
                    return f.get();
                } catch (InterruptedException | ExecutionException e) {
                    return false;
                }
            }).reduce(true, (r, f) -> r && f);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void uploadFiles(List<T> remoteInstances, List<Path> files) {
        try {
            List<Callable<Boolean>> tasks = remoteInstances.stream()
                    .map(instances -> (Callable<Boolean>) () -> instances.uploadFiles(files))
                    .collect(Collectors.toList());
            executorService.invokeAll(tasks);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    protected List<String> resolveCommands(T remoteInstance, List<String> commands) {
        return commands.stream()
                .map(command -> getEnvVariables(remoteInstance) + command)
                .peek(command -> logger.info("Command mapped to {}", command))
                .collect(Collectors.toList());
    }

    protected String getEnvVariables(T remoteInstance) {
        return String.format("export ROOT_NODE=%s NODE=%s PEER_ID=%d; ", rootInstance.getHost(), remoteInstance.getHost(), remoteInstance.getId());
    }

    public void stop() {
        executorService.shutdown();
    }
}
