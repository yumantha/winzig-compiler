package com.yumantha;

import com.sun.xml.internal.bind.v2.TODO;
import com.yumantha.errors.ParseError;

import java.util.ArrayList;

public class Parser {
    public static void parseStatement(ArrayList<Token> input) {
        Parser parser = new Parser(input);
        parser.parseWinzig();
        parser.readToken(Token.Type.EOF);
    }

    private ArrayList<Token> input;
    private int inputIndex;
    private Token currentToken;
    private Token eof;

    private Parser(ArrayList<Token> input) {
        this.input = input;
        this.inputIndex = 0;

        if (input.isEmpty()) {
            this.eof = new Token(Token.Type.EOF, "<EOF>", 0, 0);
        } else {
            Token last = input.get(input.size() - 1);
            this.eof = new Token(Token.Type.EOF, "<EOF>", last.line, last.endCol);
        }
    }

    private void parseWinzig() {
        int n = 1;

        currentToken = peek();

        if (currentToken.t_type == Token.Type.PROG) {
            readToken(Token.Type.PROG);
            parseName();
            readToken(Token.Type.COLON);
            parseConsts();
            parseTypes();
            parseDclns();
            parseSubProgs();
            parseBody();
            parseName();
            readToken(Token.Type.DOT);

            buildTree("program", n);
        } else {
            throw new ParseError("Parse error near line: " + currentToken.line + " col: " + currentToken.col + " \nExpected: " + Token.Type.PROG);
        }

    }

    private void parseConsts() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.CONST) {
            readToken(Token.Type.CONST);
            parseConst();

            while (currentToken.t_type == Token.Type.COMMA) {
                readToken(Token.Type.COMMA);
                parseConst();
            }

            readToken(Token.Type.SEMI_COLON);

            buildTree("consts", 123);
        } else {
            buildTree("consts", 0);
        }
    }

    private void parseConst() {
        parseName();
        readToken(Token.Type.EQUAL_OP);
        parseConstValue();

        buildTree("const", 123);
    }

    private void parseConstValue() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.INTEGER) {
            readToken(Token.Type.INTEGER);
        } else if (currentToken.t_type == Token.Type.CHAR) {
            readToken(Token.Type.CHAR);
        } else if (currentToken.t_type == Token.Type.IDENTIFIER) {
            parseName();
        } else {
            throw new ParseError("Parse error near line: " + currentToken.line + " col: " + currentToken.col + " \nExpected: " + Token.Type.INTEGER + ", " + Token.Type.CHAR + " or " + Token.Type.IDENTIFIER);
        }
    }

    private void parseTypes() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.TYPE) {
            readToken(Token.Type.TYPE);
            parseType();
            readToken(Token.Type.SEMI_COLON);

            while (currentToken.t_type == Token.Type.IDENTIFIER) {
                parseType();
                readToken(Token.Type.SEMI_COLON);
            }

            buildTree("types", 123);
        } else {
            buildTree("types", 0);
        }

    }

    private void parseType() {
        parseName();
        readToken(Token.Type.EQUAL_OP);
        parseLitList();
    }

    private void parseLitList() {
        readToken(Token.Type.LPAREN);
        parseName();

        while (currentToken.t_type == Token.Type.COMMA) {
            readToken(Token.Type.COMMA);
            parseName();
        }

        readToken(Token.Type.RPAREN);

        buildTree("lit", 123);
    }

    private void parseSubProgs() {
        while (currentToken.t_type == Token.Type.FUNCTION) {
            parseFcn();
        }

        buildTree("subprogs", 123);
    }

    private void parseFcn() {
        readToken(Token.Type.FUNCTION);
        parseName();
        readToken(Token.Type.LPAREN);
        parseParams();
        readToken(Token.Type.RPAREN);
        readToken(Token.Type.COLON);
        parseName();
        readToken(Token.Type.SEMI_COLON);
        parseConsts();
        parseTypes();
        parseDclns();
        parseBody();
        parseName();
        readToken(Token.Type.SEMI_COLON);

        buildTree("fcn", 123);
    }

    private void parseParams() {
        parseDcln();

        while (currentToken.t_type == Token.Type.SEMI_COLON) {
            readToken(Token.Type.SEMI_COLON);
            parseDcln();
        }

        buildTree("params", 123);
    }

    private void parseDclns() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.VAR) {
            readToken(Token.Type.VAR);
            parseDcln();
            readToken(Token.Type.SEMI_COLON);

            while (currentToken.t_type == Token.Type.IDENTIFIER) {
                parseDcln();
                readToken(Token.Type.SEMI_COLON);
            }

            buildTree("dclns", 123);
        } else {
            buildTree("dclns", 0);
        }
    }

    private void parseDcln() {
        parseName();

        while (currentToken.t_type == Token.Type.COMMA) {
            readToken(Token.Type.COMMA);
            parseName();
        }

        readToken(Token.Type.COLON);
        parseName();

        buildTree("var", 123);
    }

    private void parseBody() {
        readToken(Token.Type.BEGIN);
        parseStatement();

        while (currentToken.t_type == Token.Type.SEMI_COLON) {
            readToken(Token.Type.SEMI_COLON);
            parseStatement();
        }

        readToken(Token.Type.END);

        buildTree("block", 123);
    }

    private void parseStatement() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.IDENTIFIER) {
            parseAssignment();

        } else if (currentToken.t_type == Token.Type.OUTPUT) {
            readToken(Token.Type.OUTPUT);
            readToken(Token.Type.LPAREN);
            parseOutExp();

            while (currentToken.t_type == Token.Type.COMMA) {
                readToken(Token.Type.COMMA);
                parseOutExp();
            }

            readToken(Token.Type.RPAREN);

            buildTree("output", 123);

        } else if (currentToken.t_type == Token.Type.IF) {
            readToken(Token.Type.IF);
            parseExpression();
            readToken(Token.Type.THEN);
            parseStatement();

            if (currentToken.t_type == Token.Type.ELSE) {
                readToken(Token.Type.ELSE);
                parseStatement();
            }

            buildTree("if", 123);

        } else if (currentToken.t_type == Token.Type.WHILE) {
            readToken(Token.Type.WHILE);
            parseExpression();
            readToken(Token.Type.DO);
            parseStatement();

            buildTree("while", 123);

        } else if (currentToken.t_type == Token.Type.REPEAT) {
            readToken(Token.Type.REPEAT);
            parseStatement();

            while (currentToken.t_type == Token.Type.SEMI_COLON) {
                readToken(Token.Type.SEMI_COLON);
                parseStatement();
            }

            readToken(Token.Type.UNTIL);
            parseExpression();

            buildTree("repeat", 123);

        } else if (currentToken.t_type == Token.Type.FOR) {
            readToken(Token.Type.FOR);
            readToken(Token.Type.LPAREN);
            parseForStat();
            readToken(Token.Type.SEMI_COLON);
            parseForExp();
            readToken(Token.Type.SEMI_COLON);
            parseForStat();
            readToken(Token.Type.RPAREN);
            parseStatement();

            buildTree("for", 123);

        } else if (currentToken.t_type == Token.Type.LOOP) {
            readToken(Token.Type.LOOP);
            parseStatement();

            while (currentToken.t_type == Token.Type.SEMI_COLON) {
                readToken(Token.Type.SEMI_COLON);
                parseStatement();
            }

            readToken(Token.Type.POOL);

            buildTree("loop", 123);

        } else if (currentToken.t_type == Token.Type.CASE) {
            readToken(Token.Type.CASE);
            parseExpression();
            readToken(Token.Type.OF);
            parseCaseClauses();
            parseOtherwiseClause();
            readToken(Token.Type.END);

            buildTree("case", 123);

        } else if (currentToken.t_type == Token.Type.READ) {
            readToken(Token.Type.READ);
            readToken(Token.Type.LPAREN);
            parseName();

            while (currentToken.t_type == Token.Type.COMMA) {
                readToken(Token.Type.COMMA);
                parseName();
            }

            readToken(Token.Type.RPAREN);

            buildTree("read", 123);

        } else if (currentToken.t_type == Token.Type.EXIT) {
            readToken(Token.Type.EXIT);

            buildTree("exit", 123);

        } else if (currentToken.t_type == Token.Type.RETURN) {
            readToken(Token.Type.RETURN);
            parseExpression();

            buildTree("return", 123);

        } else if (currentToken.t_type == Token.Type.BEGIN) {
            parseBody();

        } else {
            buildTree("<null>", 0);
        }

    }

    private void parseOutExp() {
        currentToken = peek();

        if ((currentToken.t_type == Token.Type.MINUS_OP) ||
                (currentToken.t_type == Token.Type.PLUS_OP) ||
                (currentToken.t_type == Token.Type.NOT_OP) ||
                (currentToken.t_type == Token.Type.EOF) ||
                (currentToken.t_type == Token.Type.IDENTIFIER) ||
                (currentToken.t_type == Token.Type.INTEGER) ||
                (currentToken.t_type == Token.Type.CHAR) ||
                (currentToken.t_type == Token.Type.LPAREN) ||
                (currentToken.t_type == Token.Type.SUCC) ||
                (currentToken.t_type == Token.Type.PRED) ||
                (currentToken.t_type == Token.Type.CHR) ||
                (currentToken.t_type == Token.Type.ORD)) {
            parseExpression();

            buildTree("integer", 123);
        } else if (currentToken.t_type == Token.Type.STRING) {
            parseStringNode();

            buildTree("string", 123);
        } else {
            throw new ParseError("Parse error near line: " + currentToken.line + " col: " + currentToken.col + " \nExpected: "
                    + Token.Type.MINUS_OP + ", "
                    + Token.Type.PLUS_OP + ", "
                    + Token.Type.NOT_OP + ", "
                    + Token.Type.EOF + ", "
                    + Token.Type.IDENTIFIER + ", "
                    + Token.Type.INTEGER + ", "
                    + Token.Type.CHAR + ", "
                    + Token.Type.LPAREN + ", "
                    + Token.Type.SUCC + ", "
                    + Token.Type.PRED + ", "
                    + Token.Type.CHR + ", "
                    + Token.Type.ORD + " or "
                    + Token.Type.STRING
            );
        }
    }

    private void parseStringNode() {
        readToken(Token.Type.STRING);
    }

    private void parseCaseClauses() {
        parseCaseClause();
        readToken(Token.Type.SEMI_COLON);

        while ((currentToken.t_type == Token.Type.INTEGER) ||
                (currentToken.t_type == Token.Type.CHAR) ||
                (currentToken.t_type == Token.Type.IDENTIFIER)) {
            parseCaseClause();
            readToken(Token.Type.SEMI_COLON);
        }
    }

    private void parseCaseClause() {
        parseCaseExpression();

        while (currentToken.t_type == Token.Type.COMMA) {
            readToken(Token.Type.COMMA);
            parseCaseExpression();
        }

        readToken(Token.Type.COLON);
        parseStatement();

        buildTree("case_clause", 123);
    }

    private void parseCaseExpression() {
        parseConstValue();

        if (currentToken.t_type == Token.Type.CASE_DOTS) {
            readToken(Token.Type.CASE_DOTS);
            parseConstValue();

            buildTree("..", 123);
        }
    }

    private void parseOtherwiseClause() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.OTHERWISE) {
            readToken(Token.Type.OTHERWISE);
            parseStatement();

            buildTree("otherwise", 123);

        }
    }

    private void parseAssignment() {
        parseName();

        if (currentToken.t_type == Token.Type.ASSIGN) {
            readToken(Token.Type.ASSIGN);
            parseExpression();

            buildTree("assign", 123);

        } else if (currentToken.t_type == Token.Type.SWAP) {
            readToken(Token.Type.SWAP);
            parseName();

            buildTree("swap", 123);

        } else {
            throw new ParseError("Parse error near line: " + currentToken.line + " col: " + currentToken.col + " \nExpected: " + Token.Type.ASSIGN + " or " + Token.Type.SWAP);
        }
    }

    private void parseForStat() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.IDENTIFIER) {
            parseAssignment();
        } else {
            buildTree("<null>", 123);
        }
    }

    private void parseForExp() {
        if ((currentToken.t_type == Token.Type.MINUS_OP) ||
                (currentToken.t_type == Token.Type.PLUS_OP) ||
                (currentToken.t_type == Token.Type.NOT_OP) ||
                (currentToken.t_type == Token.Type.EOF) ||
                (currentToken.t_type == Token.Type.IDENTIFIER) ||
                (currentToken.t_type == Token.Type.INTEGER) ||
                (currentToken.t_type == Token.Type.CHAR) ||
                (currentToken.t_type == Token.Type.LPAREN) ||
                (currentToken.t_type == Token.Type.SUCC) ||
                (currentToken.t_type == Token.Type.PRED) ||
                (currentToken.t_type == Token.Type.CHR) ||
                (currentToken.t_type == Token.Type.ORD)) {
            parseExpression();

        } else {
            buildTree("true", 123);
        }
    }

    private void parseExpression() {
        parseTerm();

        if (currentToken.t_type == Token.Type.LESS_EQUAL_OP) {
            readToken(Token.Type.GREATER_EQUAL_OP);
            parseTerm();

            buildTree("<=", 123);

        } else if (currentToken.t_type == Token.Type.LESS_OP) {
            readToken(Token.Type.GREATER_OP);
            parseTerm();

            buildTree("<", 123);

        } else if (currentToken.t_type == Token.Type.GREATER_EQUAL_OP) {
            readToken(Token.Type.GREATER_EQUAL_OP);
            parseTerm();

            buildTree(">=", 123);

        } else if (currentToken.t_type == Token.Type.GREATER_OP) {
            readToken(Token.Type.GREATER_OP);
            parseTerm();

            buildTree(">=", 123);

        } else if (currentToken.t_type == Token.Type.EQUAL_OP) {
            readToken(Token.Type.EQUAL_OP);
            parseTerm();

            buildTree("=", 123);

        } else if (currentToken.t_type == Token.Type.NOT_EQUAL_OP) {
            readToken(Token.Type.NOT_EQUAL_OP);
            parseTerm();

            buildTree("<>", 123);

        }
    }

    private void parseTerm() {
        parseFactor();

        while ((currentToken.t_type == Token.Type.PLUS_OP) ||
                (currentToken.t_type == Token.Type.MINUS_OP) ||
                (currentToken.t_type == Token.Type.OR_OP)) {
            if (currentToken.t_type == Token.Type.PLUS_OP) {
                readToken(Token.Type.PLUS_OP);
                parseFactor();

                buildTree("+", 123);
            } else if (currentToken.t_type == Token.Type.MINUS_OP) {
                readToken(Token.Type.MINUS_OP);
                parseFactor();

                buildTree("-", 123);
            } else if (currentToken.t_type == Token.Type.OR_OP) {
                readToken(Token.Type.OR_OP);
                parseFactor();

                buildTree("or", 123);
            }
        }
    }

    private void parseFactor() {
        parsePrimary();

        while ((currentToken.t_type == Token.Type.MULTIPLY_OP) ||
                (currentToken.t_type == Token.Type.DIVIDE_OP) ||
                (currentToken.t_type == Token.Type.AND_OP) ||
                (currentToken.t_type == Token.Type.MOD_OP)) {
            if (currentToken.t_type == Token.Type.MULTIPLY_OP) {
                readToken(Token.Type.MULTIPLY_OP);
                parsePrimary();

                buildTree("*", 123);
            } else if (currentToken.t_type == Token.Type.DIVIDE_OP) {
                readToken(Token.Type.DIVIDE_OP);
                parsePrimary();

                buildTree("/", 123);
            } else if (currentToken.t_type == Token.Type.AND_OP) {
                readToken(Token.Type.AND_OP);
                parsePrimary();

                buildTree("and", 123);
            } else if (currentToken.t_type == Token.Type.MOD_OP) {
                readToken(Token.Type.MOD_OP);
                parsePrimary();

                buildTree("mod", 123);
            }
        }
    }

    private void parsePrimary() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.IDENTIFIER) {
            parseName();

            if (currentToken.t_type == Token.Type.LPAREN) {
                readToken(Token.Type.LPAREN);
                parseExpression();

                while (currentToken.t_type == Token.Type.COMMA) {
                    readToken(Token.Type.COMMA);
                    parseExpression();
                }

                readToken(Token.Type.RPAREN);

                buildTree("call", 123);
            }
        } else if (currentToken.t_type == Token.Type.MINUS_OP) {
            readToken(Token.Type.MINUS_OP);
            parsePrimary();

            buildTree("-", 123);

        } else if (currentToken.t_type == Token.Type.PLUS_OP) {
            readToken(Token.Type.PLUS_OP);
            parsePrimary();

        } else if (currentToken.t_type == Token.Type.NOT_OP) {
            readToken(Token.Type.NOT_OP);
            parsePrimary();

            buildTree("not", 123);

        } else if (currentToken.t_type == Token.Type.EOF) {
            readToken(Token.Type.EOF);

            buildTree("eof", 123);

        } else if (currentToken.t_type == Token.Type.INTEGER) {
            readToken(Token.Type.INTEGER);

        } else if (currentToken.t_type == Token.Type.CHAR) {
            readToken(Token.Type.CHAR);

        } else if (currentToken.t_type == Token.Type.LPAREN) {
            readToken(Token.Type.LPAREN);
            parseExpression();
            readToken(Token.Type.RPAREN);

        } else if (currentToken.t_type == Token.Type.SUCC) {
            readToken(Token.Type.SUCC);
            readToken(Token.Type.LPAREN);
            parseExpression();
            readToken(Token.Type.RPAREN);

            buildTree("succ", 123);

        } else if (currentToken.t_type == Token.Type.PRED) {
            readToken(Token.Type.PRED);
            readToken(Token.Type.LPAREN);
            parseExpression();
            readToken(Token.Type.RPAREN);

            buildTree("pred", 123);

        } else if (currentToken.t_type == Token.Type.CHR) {
            readToken(Token.Type.CHR);
            readToken(Token.Type.LPAREN);
            parseExpression();
            readToken(Token.Type.RPAREN);

            buildTree("chr", 123);

        } else if (currentToken.t_type == Token.Type.ORD) {
            readToken(Token.Type.ORD);
            readToken(Token.Type.LPAREN);
            parseExpression();
            readToken(Token.Type.RPAREN);

            buildTree("ord", 123);

        }
    }

    private void parseName() {
        readToken(Token.Type.IDENTIFIER);
    }

    private Token peekAtOffset(int offset) {
        if (inputIndex + offset < input.size()) {
            return input.get(inputIndex + offset);
        }

        return eof;
    }

    private Token peek() {
        return peekAtOffset(0);
    }

    private void readToken(Token.Type expType) {
        if (currentToken.t_type == expType) {
            inputIndex++;
            currentToken = peek();
        } else {
            throw new ParseError("Parse error near line: " + currentToken.line + " col: " + currentToken.col + " \nExpected " + expType);
        }
    }

    private void buildTree(String ruleName, int n) {
        System.out.println(ruleName);
        System.out.println(n);
        System.out.println();
    }
}
