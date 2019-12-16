package com.yumantha;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        String inputPath = "winzig_test_programs/";
        String outputPath = "output/";

        for (int i = 1; i <= 15; i++) {
            String iStr;
            if (i < 10) {
                iStr = "0" + i;
            } else {
                iStr = Integer.toString(i);
            }

            String inFile = inputPath + "winzig_" + iStr;
            String outFile = outputPath + "winzig_" + iStr + ".tree";

            Compiler.compile(inFile, outFile);
            System.out.println("------------------------------------------------------------");
        }
    }
}
