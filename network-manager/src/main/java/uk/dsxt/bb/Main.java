package uk.dsxt.bb;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) {
        StringBuffer output = new StringBuffer();

        try {
            System.out.println("adding ip");
            exec("sudo iptables -I INPUT -s 1.2.3.4 -j DROP", output);

            int delay_millis = (1 + ThreadLocalRandom.current().nextInt(6)) * 30000;
            System.out.println("sleeping for: " + delay_millis);
            Thread.sleep(delay_millis);

            System.out.println("removing ip");
            exec("sudo iptables -D INPUT 1", output);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void exec(String command, StringBuffer output) {
        try {
            Process p = Runtime.getRuntime().exec("sudo iptables -I INPUT -s 1.2.3.4 -j DROP");
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
