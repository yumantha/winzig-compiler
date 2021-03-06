package com.yumantha;

import java.io.IOException;
import java.util.ArrayList;

public class Compiler {
    public static void compile(String inputPath) throws IOException {
        FileHandler fileHandler = new FileHandler();
        String sourceCode = fileHandler.readFile(inputPath);
        ArrayList<Token> tokens = Scanner.scan(sourceCode);

//        for (Token token : tokens) {
//            System.out.println(token);
//        }

        ASTNode prog = Parser.parseWinzig(tokens);

        if (prog != null) {
            prog.inOrderTraverse();

//            fileHandler.writeAST(outputPath, prog);
        } else {
            throw new RuntimeException("Parse Error!");
        }
    }
}
