package com.yumantha;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileHandler {
    public String readFile(String inputPath) throws IOException {
        File f = new File(inputPath);
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);

        int c = 0;
        StringBuilder sourceCode = new StringBuilder();

        while ((c = br.read()) != -1) {
            char character = (char) c;
            sourceCode.append(character);
        }

        return sourceCode.toString();
    }
}
