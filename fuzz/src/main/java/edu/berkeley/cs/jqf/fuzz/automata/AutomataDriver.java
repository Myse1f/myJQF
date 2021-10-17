package edu.berkeley.cs.jqf.fuzz.automata; /**
 * Created By Yufan Wu
 * 2021/10/16
 */

import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import org.junit.runner.Result;

import java.io.File;
import java.util.Random;

public class AutomataDriver {
    public static void main(String[] args) {
        if (args.length < 3){
            System.err.println("Usage: java " + AutomataDriver.class + " TEST_CLASS TEST_METHOD AUTOMATA_FILE [OUTPUT_DIR]");
            System.exit(1);
        }

        String testClassName  = args[0];
        String testMethodName = args[1];
        String automataFile = args[2];
        String outputDirectoryName = args.length > 3 ? args[3] : "fuzz-results";
        File outputDirectory = new File(outputDirectoryName);
        File[] seedFiles = null;
        if (args.length > 4) {
            seedFiles = new File[args.length-4];
            for (int i = 3; i < args.length; i++) {
                seedFiles[i-3] = new File(args[i]);
            }
        }

        try {
            // Load the guidance
            String title = testClassName+"#"+testMethodName;
            AutomataGuidance guidance = new AutomataGuidance(title, null, null, outputDirectory, new Random(), automataFile);

            // Run the Junit test
            Result res = GuidedFuzzing.run(testClassName, testMethodName, guidance, System.out);
            if (Boolean.getBoolean("jqf.logCoverage")) {
                System.out.println(String.format("Covered %d edges.",
                        guidance.getTotalCoverage().getNonZeroCount()));
            }
            if (Boolean.getBoolean("jqf.ei.EXIT_ON_CRASH") && !res.wasSuccessful()) {
                System.exit(3);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

    }
}
