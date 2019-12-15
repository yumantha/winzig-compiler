package com.yumantha;

import com.yumantha.errors.ParseError;

import java.util.ArrayList;
import java.util.Stack;

public class Parser {
    public static ASTNode parseProgram(ArrayList<Token> input) {
        Parser parser = new Parser(input);
        parser.parseWinzig();
        parser.readToken(Token.Type.EOF);

        if (parser.stack.size() == 1) {
            return parser.stack.get(0);
        } else {
            return null;
        }
    }

    private ArrayList<Token> input;
    private int inputIndex;
    private Token currentToken;
    private Token eof;
    private Stack<ASTNode> stack;

    private Parser(ArrayList<Token> input) {
        this.input = input;
        this.inputIndex = 0;

        if (input.isEmpty()) {
            this.eof = new Token(Token.Type.EOF, "<EOF>", 0, 0);
        } else {
            Token last = input.get(input.size() - 1);
            this.eof = new Token(Token.Type.EOF, "<EOF>", last.line, last.endCol);
        }

        this.stack = new Stack<ASTNode>();
    }

    private void parseWinzig() {
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

            buildTree("program", 7);
        } else {
            throw new ParseError("Parse error near line: " + currentToken.line + " col: " + currentToken.col + " \nExpected: " + Token.Type.PROG);
        }

    }

    private void parseConsts() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.CONST) {
            int n = 0;

            readToken(Token.Type.CONST);
            parseConst();
            n += 1;

            while (currentToken.t_type == Token.Type.COMMA) {
                readToken(Token.Type.COMMA);
                parseConst();
                n += 1;
            }

            readToken(Token.Type.SEMI_COLON);

            buildTree("consts", n);
        } else {
            buildTree("consts", 0);
        }
    }

    private void parseConst() {
        parseName();
        readToken(Token.Type.EQUAL_OP);
        parseConstValue();

        buildTree("const", 2);
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
            int n = 0;

            readToken(Token.Type.TYPE);
            parseType();
            n += 1;
            readToken(Token.Type.SEMI_COLON);

            while (currentToken.t_type == Token.Type.IDENTIFIER) {
                parseType();
                n += 1;
                readToken(Token.Type.SEMI_COLON);
            }

            buildTree("types", n);
        } else {
            buildTree("types", 0);
        }
    }

    private void parseType() {
        parseName();
        readToken(Token.Type.EQUAL_OP);
        parseLitList();

        buildTree("type", 2);
    }

    private void parseLitList() {
        int n = 0;

        readToken(Token.Type.LPAREN);
        parseName();
        n += 1;

        while (currentToken.t_type == Token.Type.COMMA) {
            readToken(Token.Type.COMMA);
            parseName();
            n += 1;
        }

        readToken(Token.Type.RPAREN);

        buildTree("lit", n);
    }

    private void parseSubProgs() {
        int n = 0;

        while (currentToken.t_type == Token.Type.FUNCTION) {
            parseFcn();
            n += 1;
        }

        buildTree("subprogs", n);
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

        buildTree("fcn", 8);
    }

    private void parseParams() {
        int n = 0;

        parseDcln();
        n += 1;

        while (currentToken.t_type == Token.Type.SEMI_COLON) {
            readToken(Token.Type.SEMI_COLON);
            parseDcln();
            n += 1;
        }

        buildTree("params", n);
    }

    private void parseDclns() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.VAR) {
            int n = 0;

            readToken(Token.Type.VAR);
            parseDcln();
            n += 1;
            readToken(Token.Type.SEMI_COLON);

            while (currentToken.t_type == Token.Type.IDENTIFIER) {
                parseDcln();
                n += 1;
                readToken(Token.Type.SEMI_COLON);
            }

            buildTree("dclns", n);
        } else {
            buildTree("dclns", 0);
        }
    }

    private void parseDcln() {
        int n = 0;

        parseName();
        n += 1;

        while (currentToken.t_type == Token.Type.COMMA) {
            readToken(Token.Type.COMMA);
            parseName();
            n += 1;
        }

        readToken(Token.Type.COLON);
        parseName();
        n += 1;

        buildTree("var", n);
    }

    private void parseBody() {
        int n = 0;

        readToken(Token.Type.BEGIN);
        parseProgram();
        n += 1;

        while (currentToken.t_type == Token.Type.SEMI_COLON) {
            readToken(Token.Type.SEMI_COLON);
            parseProgram();
            n += 1;
        }

        readToken(Token.Type.END);

        buildTree("block", n);
    }

    private void parseProgram() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.IDENTIFIER) {
            parseAssignment();
        } else if (currentToken.t_type == Token.Type.OUTPUT) {
            int n = 0;

            readToken(Token.Type.OUTPUT);
            readToken(Token.Type.LPAREN);
            parseOutExp();
            n += 1;

            while (currentToken.t_type == Token.Type.COMMA) {
                readToken(Token.Type.COMMA);
                parseOutExp();
                n += 1;
            }

            readToken(Token.Type.RPAREN);

            buildTree("output", n);
        } else if (currentToken.t_type == Token.Type.IF) {
            int n = 0;

            readToken(Token.Type.IF);
            parseExpression();
            n += 1;
            readToken(Token.Type.THEN);
            parseProgram();
            n += 1;

            if (currentToken.t_type == Token.Type.ELSE) {
                readToken(Token.Type.ELSE);
                parseProgram();
                n += 1;
            }

            buildTree("if", n);
        } else if (currentToken.t_type == Token.Type.WHILE) {
            readToken(Token.Type.WHILE);
            parseExpression();
            readToken(Token.Type.DO);
            parseProgram();

            buildTree("while", 2);
        } else if (currentToken.t_type == Token.Type.REPEAT) {
            int n = 0;

            readToken(Token.Type.REPEAT);
            parseProgram();
            n += 1;

            while (currentToken.t_type == Token.Type.SEMI_COLON) {
                readToken(Token.Type.SEMI_COLON);
                parseProgram();
                n += 1;
            }

            readToken(Token.Type.UNTIL);
            parseExpression();
            n += 1;

            buildTree("repeat", n);
        } else if (currentToken.t_type == Token.Type.FOR) {
            readToken(Token.Type.FOR);
            readToken(Token.Type.LPAREN);
            parseForStat();
            readToken(Token.Type.SEMI_COLON);
            parseForExp();
            readToken(Token.Type.SEMI_COLON);
            parseForStat();
            readToken(Token.Type.RPAREN);
            parseProgram();

            buildTree("for", 4);
        } else if (currentToken.t_type == Token.Type.LOOP) {
            int n = 0;

            readToken(Token.Type.LOOP);
            parseProgram();
            n += 1;

            while (currentToken.t_type == Token.Type.SEMI_COLON) {
                readToken(Token.Type.SEMI_COLON);
                parseProgram();
                n += 1;
            }

            readToken(Token.Type.POOL);

            buildTree("loop", n);
        } else if (currentToken.t_type == Token.Type.CASE) {
            readToken(Token.Type.CASE);
            parseExpression();
            readToken(Token.Type.OF);
            parseCaseClauses();
            parseOtherwiseClause();
            readToken(Token.Type.END);

            buildTree("case", 3);
        } else if (currentToken.t_type == Token.Type.READ) {
            int n = 0;

            readToken(Token.Type.READ);
            readToken(Token.Type.LPAREN);
            parseName();
            n += 1;

            while (currentToken.t_type == Token.Type.COMMA) {
                readToken(Token.Type.COMMA);
                parseName();
                n += 1;
            }

            readToken(Token.Type.RPAREN);

            buildTree("read", n);
        } else if (currentToken.t_type == Token.Type.EXIT) {
            readToken(Token.Type.EXIT);

            buildTree("exit", 0);
        } else if (currentToken.t_type == Token.Type.RETURN) {
            readToken(Token.Type.RETURN);
            parseExpression();

            buildTree("return", 1);
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

            buildTree("integer", 1);
        } else if (currentToken.t_type == Token.Type.STRING) {
            parseStringNode();

            buildTree("string", 1);
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
        int n = 0;

        parseCaseExpression();
        n += 1;

        while (currentToken.t_type == Token.Type.COMMA) {
            readToken(Token.Type.COMMA);
            parseCaseExpression();
            n += 1;
        }

        readToken(Token.Type.COLON);
        parseProgram();
        n += 1;

        buildTree("case_clause", n);
    }

    private void parseCaseExpression() {
        parseConstValue();

        if (currentToken.t_type == Token.Type.CASE_DOTS) {
            readToken(Token.Type.CASE_DOTS);
            parseConstValue();

            buildTree("..", 2);
        }
    }

    private void parseOtherwiseClause() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.OTHERWISE) {
            readToken(Token.Type.OTHERWISE);
            parseProgram();

            buildTree("otherwise", 1);
        }
    }

    private void parseAssignment() {
        parseName();

        if (currentToken.t_type == Token.Type.ASSIGN) {
            readToken(Token.Type.ASSIGN);
            parseExpression();

            buildTree("assign", 2);
        } else if (currentToken.t_type == Token.Type.SWAP) {
            readToken(Token.Type.SWAP);
            parseName();

            buildTree("swap", 2);
        } else {
            throw new ParseError("Parse error near line: " + currentToken.line + " col: " + currentToken.col + " \nExpected: " + Token.Type.ASSIGN + " or " + Token.Type.SWAP);
        }
    }

    private void parseForStat() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.IDENTIFIER) {
            parseAssignment();
        } else {
            buildTree("<null>", 0);
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
            buildTree("true", 0);
        }
    }

    private void parseExpression() {
        parseTerm();

        if (currentToken.t_type == Token.Type.LESS_EQUAL_OP) {
            readToken(Token.Type.GREATER_EQUAL_OP);
            parseTerm();

            buildTree("<=", 2);
        } else if (currentToken.t_type == Token.Type.LESS_OP) {
            readToken(Token.Type.GREATER_OP);
            parseTerm();

            buildTree("<", 2);
        } else if (currentToken.t_type == Token.Type.GREATER_EQUAL_OP) {
            readToken(Token.Type.GREATER_EQUAL_OP);
            parseTerm();

            buildTree(">=", 2);
        } else if (currentToken.t_type == Token.Type.GREATER_OP) {
            readToken(Token.Type.GREATER_OP);
            parseTerm();

            buildTree(">=", 2);
        } else if (currentToken.t_type == Token.Type.EQUAL_OP) {
            readToken(Token.Type.EQUAL_OP);
            parseTerm();

            buildTree("=", 2);
        } else if (currentToken.t_type == Token.Type.NOT_EQUAL_OP) {
            readToken(Token.Type.NOT_EQUAL_OP);
            parseTerm();

            buildTree("<>", 2);
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

                buildTree("+", 2);
            } else if (currentToken.t_type == Token.Type.MINUS_OP) {
                readToken(Token.Type.MINUS_OP);
                parseFactor();

                buildTree("-", 2);
            } else if (currentToken.t_type == Token.Type.OR_OP) {
                readToken(Token.Type.OR_OP);
                parseFactor();

                buildTree("or", 2);
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

                buildTree("*", 2);
            } else if (currentToken.t_type == Token.Type.DIVIDE_OP) {
                readToken(Token.Type.DIVIDE_OP);
                parsePrimary();

                buildTree("/", 2);
            } else if (currentToken.t_type == Token.Type.AND_OP) {
                readToken(Token.Type.AND_OP);
                parsePrimary();

                buildTree("and", 2);
            } else if (currentToken.t_type == Token.Type.MOD_OP) {
                readToken(Token.Type.MOD_OP);
                parsePrimary();

                buildTree("mod", 2);
            }
        }
    }

    private void parsePrimary() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.IDENTIFIER) {
            int n = 0;

            parseName();
            n += 1;

            if (currentToken.t_type == Token.Type.LPAREN) {
                readToken(Token.Type.LPAREN);
                parseExpression();
                n += 1;

                while (currentToken.t_type == Token.Type.COMMA) {
                    readToken(Token.Type.COMMA);
                    parseExpression();
                    n += 1;
                }

                readToken(Token.Type.RPAREN);

                buildTree("call", n);
            }
        } else if (currentToken.t_type == Token.Type.MINUS_OP) {
            readToken(Token.Type.MINUS_OP);
            parsePrimary();

            buildTree("-", 1);
        } else if (currentToken.t_type == Token.Type.PLUS_OP) {
            readToken(Token.Type.PLUS_OP);
            parsePrimary();
        } else if (currentToken.t_type == Token.Type.NOT_OP) {
            readToken(Token.Type.NOT_OP);
            parsePrimary();

            buildTree("not", 1);
        } else if (currentToken.t_type == Token.Type.EOF) {
            readToken(Token.Type.EOF);

            buildTree("eof", 0);
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

            buildTree("succ", 1);
        } else if (currentToken.t_type == Token.Type.PRED) {
            readToken(Token.Type.PRED);
            readToken(Token.Type.LPAREN);
            parseExpression();
            readToken(Token.Type.RPAREN);

            buildTree("pred", 1);
        } else if (currentToken.t_type == Token.Type.CHR) {
            readToken(Token.Type.CHR);
            readToken(Token.Type.LPAREN);
            parseExpression();
            readToken(Token.Type.RPAREN);

            buildTree("chr", 1);
        } else if (currentToken.t_type == Token.Type.ORD) {
            readToken(Token.Type.ORD);
            readToken(Token.Type.LPAREN);
            parseExpression();
            readToken(Token.Type.RPAREN);

            buildTree("ord", 1);
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
            if (currentToken.t_type == Token.Type.IDENTIFIER) {
                buildTree(currentToken.text, 0);
                buildTree("<identifier>", 1);
            } else if (currentToken.t_type == Token.Type.STRING) {
                buildTree(currentToken.text, 0);
                buildTree("<string>", 1);
            } else if (currentToken.t_type == Token.Type.INTEGER) {
                buildTree(currentToken.text, 0);
                buildTree("<integer>", 1);
            } else if (currentToken.t_type == Token.Type.CHAR) {
                buildTree(currentToken.text, 0);
                buildTree("<char>", 1);
            }

            inputIndex++;
            currentToken = peek();
        } else {
            throw new ParseError("Parse error near line: " + currentToken.line + " col: " + currentToken.col + " \nExpected " + expType);
        }
    }

    private void buildTree(String ruleName, int n) {
        ASTNode node = new ASTNode(ruleName, n);

        for (int i = 0; i < n; i++) {
            ASTNode childNode = stack.pop();
            childNode.setParent(node);

            node.addChild(childNode);
        }

        node.reverseChildren();
        stack.push(node);
    }
}
