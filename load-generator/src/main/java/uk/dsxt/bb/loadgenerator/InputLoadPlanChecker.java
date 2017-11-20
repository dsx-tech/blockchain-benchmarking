package uk.dsxt.bb.loadgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.dsxt.bb.loadgenerator.load_plan.LoadPlan;

import java.io.File;
import java.io.IOException;

public class InputLoadPlanChecker {
    /**
     * You can call it to check validity of your load configuration file before submitting it to network
     *
     * @param args One argument: Path to load configuration file
     */
    public static void main(String[] args) {
        check(args[0]);
    }

    private static boolean check(String filePath) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            final LoadPlan loadPlan = objectMapper.readValue(new File(filePath), LoadPlan.class);
            System.out.println("Successfully read load plan: " + loadPlan);
        } catch (IOException e) {
            System.out.println("Error during reading load plan: " + e.getMessage());
            return false;
        }
        return true;
    }
}
