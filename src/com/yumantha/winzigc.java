package com.yumantha;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class winzigc {
    private static final String usage = "Usage: java -jar winzigc.jar -ast input-file > output-file";
    private static final String invArgs = "Invalid arguments\nUse -h or --help for help";

    public static void main(String[] args) throws IOException {
        List<String> argList = Arrays.asList(args);

        String inFile = null;
        String outFile = null;

        if (argList.size() == 1) {
            if (argList.contains("-h") || argList.contains("--help")) {
                System.out.println(usage);
                System.exit(0);
            } else {
                System.out.println(invArgs);
                System.exit(1);
            }
        } else if (argList.size() == 2) {
            if (argList.get(0).equals("-ast")) {
                inFile = argList.get(1);
            } else {
                System.out.println(invArgs);
                System.exit(1);
            }
        } else {
            System.out.println(invArgs);
            System.exit(1);
        }

        Compiler.compile(inFile, outFile);
    }
}
