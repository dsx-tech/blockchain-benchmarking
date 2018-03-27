package uk.dsxt.bb;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.dsxt.bb.model.NetworkAction;
import uk.dsxt.bb.model.NetworkActionsConfig;
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
            allHosts = Files.readAllLines(Paths.get(
                    System.getProperty("user.home") + "/" + TestManager.NETWORK_MANAGER_CONFIG_PATH));
        } catch (IOException e) {
            System.out.println("Unable to load instances file, exception=" + e.getMessage());
            return;
        }

        // read network issues plan
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            networkActionsConfig = objectMapper.readValue(new File(
                    System.getProperty("user.home") + "/" + TestManager.NETWORK_MANAGER_CONFIG_PATH),
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

//            exec("sudo iptables -I INPUT -s 1.2.3.4 -j DROP");
//
//            int delay_millis = (1 + ThreadLocalRandom.current().nextInt(6)) * 30000;
//            System.out.println("sleeping for: " + delay_millis);
//            Thread.sleep(delay_millis);
//
//            System.out.println("removing ip");
//            exec("sudo iptables -D INPUT 1");
    }

    private static String exec(String command) {
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
