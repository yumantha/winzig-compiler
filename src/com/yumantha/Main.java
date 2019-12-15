package com.yumantha;

import com.yumantha.errors.ParseError;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {
        FileHandler fileHandler = new FileHandler();
        String sourceCode = fileHandler.readFile("winzig_test_programs/winzig_10");
        ArrayList<Token> tokens = Scanner.scan(sourceCode);
        ASTNode prog = Parser.parseProgram(tokens);

        if (prog != null) {
            prog.inOrderTraverse();

            fileHandler.writeAST("output/out.tree", prog);
        } else {
            throw new ParseError("Parse Error!");
        }




//        for (Token token : tokens) {
//            System.out.println(token);
//        }
    }
}
