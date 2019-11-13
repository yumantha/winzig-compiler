package com.yumantha;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {
        FileHandler fileHandler = new FileHandler();
        String sourceCode = fileHandler.readFile("winzig_test_programs/winzig_15");
        ArrayList<Token> tokens = Scanner.scan(sourceCode);

        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}
