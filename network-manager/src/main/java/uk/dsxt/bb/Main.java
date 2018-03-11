package uk.dsxt.bb;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.dsxt.bb.model.NetworkActionsConfig;
import uk.dsxt.bb.test_manager.TestManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
//        try {
            System.out.println("Starting network-manager main");
            System.out.println("Received arguments=" + Arrays.toString(args));
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
            // TODO: 11.03.2018 implement

            // read network issues plan
            final ObjectMapper objectMapper = new ObjectMapper();
            try {
                objectMapper.readValue(new File(System.getProperty("user.home") + "/" + TestManager.NETWORK_MANAGER_CONFIG_PATH),
                        NetworkActionsConfig.class);
            } catch (IOException e) {
                System.out.println("Unable to load network manager config");
                return;
            }


//            exec("sudo iptables -I INPUT -s 1.2.3.4 -j DROP");
//
//            int delay_millis = (1 + ThreadLocalRandom.current().nextInt(6)) * 30000;
//            System.out.println("sleeping for: " + delay_millis);
//            Thread.sleep(delay_millis);
//
//            System.out.println("removing ip");
//            exec("sudo iptables -D INPUT 1");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
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
