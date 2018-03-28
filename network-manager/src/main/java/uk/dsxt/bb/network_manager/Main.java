package uk.dsxt.bb.network_manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.dsxt.bb.network_manager.model.NetworkAction;
import uk.dsxt.bb.network_manager.model.NetworkActionsConfig;
import uk.dsxt.bb.test_manager.TestManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting network-manager main");
        System.out.println("Received arguments=" + Arrays.toString(args));
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        List<String> allHosts;
        NetworkActionsConfig networkActionsConfig;
        // Index of current node in list of all instances (file instances)
        int nodeIndex;
        try {
            nodeIndex = Integer.valueOf(args[0]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.printf("Unable to find nodeIndex, caught exception=%s, exiting%n", e.getMessage());
            return;
        }

        // read instances file
        try {
            // TODO: 28.03.2018 This is temporary hack, the problem is that this jar requires sudo privilegies, so user.home is /root, but the file are stored in user folder
//            allHosts = Files.readAllLines(Paths.get(System.getProperty("user.home") + "/instances"));
            allHosts = Files.readAllLines(Paths.get("/home/ubuntu/instances"));
        } catch (IOException e) {
            System.out.println("Unable to load instances file, exception=" + e.getMessage());
            return;
        }

        // read network issues plan
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            networkActionsConfig = objectMapper.readValue(
                    new File("/home/ubuntu/" + TestManager.NETWORK_MANAGER_CONFIG_PATH),
                    NetworkActionsConfig.class);
        } catch (IOException e) {
            System.out.println("Unable to load network manager config, exception=" + e.getMessage());
            return;
        }

        NetworkAction[] currentNodeNetworkActions = networkActionsConfig.getActions(nodeIndex);
        for (NetworkAction networkAction : currentNodeNetworkActions) {
            executorService.schedule(() -> networkAction.performStart(allHosts), networkAction.getStartMillis(),
                    TimeUnit.MILLISECONDS);
            executorService.schedule(() -> networkAction.performFinish(allHosts), networkAction.getFinishMillis(),
                    TimeUnit.MILLISECONDS);
        }
    }

    public static String exec(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }
}
