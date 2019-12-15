package com.yumantha;

import java.io.*;
import java.util.ArrayList;

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

    public void writeAST(String filePath, ASTNode prog) throws IOException {
        File f = new File(filePath);
        FileWriter fw = new FileWriter(f);
        BufferedWriter bw = new BufferedWriter(fw);

        prog.writeTree(bw);

        bw.close();
        fw.close();
    }
}
