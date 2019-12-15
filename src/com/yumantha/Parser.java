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
    private int level;

    private Parser(ArrayList<Token> input) {
        this.input = input;
        this.inputIndex = 0;
        this.level = 0;

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
            level--;
        } else {
            throw new ParseError("Parse error near line: " + currentToken.line + " col: " + currentToken.col + " \nExpected: " + Token.Type.PROG);
        }

    }

    private void parseConsts() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.CONST) {
            level++;
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
            level--;
        } else {
            level++;
            buildTree("consts", 0);
            level--;
        }
    }

    private void parseConst() {
        level++;
        parseName();
        readToken(Token.Type.EQUAL_OP);
        parseConstValue();

        buildTree("const", 2);
        level--;
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
            level++;
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
            level--;
        } else {
            level++;
            buildTree("types", 0);
            level--;
        }
    }

    private void parseType() {
        level++;
        parseName();
        readToken(Token.Type.EQUAL_OP);
        parseLitList();

        buildTree("type", 2);
        level--;
    }

    private void parseLitList() {
        level++;
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
        level--;
    }

    private void parseSubProgs() {
        level++;
        int n = 0;

        while (currentToken.t_type == Token.Type.FUNCTION) {
            parseFcn();
            n += 1;
        }

        buildTree("subprogs", n);
        level--;
    }

    private void parseFcn() {
        level++;
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
        level--;
    }

    private void parseParams() {
        level++;
        int n = 0;

        parseDcln();
        n += 1;

        while (currentToken.t_type == Token.Type.SEMI_COLON) {
            readToken(Token.Type.SEMI_COLON);
            parseDcln();
            n += 1;
        }

        buildTree("params", n);
        level--;
    }

    private void parseDclns() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.VAR) {
            level++;
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
            level--;
        } else {
            level++;
            buildTree("dclns", 0);
            level--;
        }
    }

    private void parseDcln() {
        level++;
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
        level--;
    }

    private void parseBody() {
        level++;
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
        level--;
    }

    private void parseProgram() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.IDENTIFIER) {
            parseAssignment();
        } else if (currentToken.t_type == Token.Type.OUTPUT) {
            level++;
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
            level--;
        } else if (currentToken.t_type == Token.Type.IF) {
            level++;
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
            level--;
        } else if (currentToken.t_type == Token.Type.WHILE) {
            level++;
            readToken(Token.Type.WHILE);
            parseExpression();
            readToken(Token.Type.DO);
            parseProgram();

            buildTree("while", 2);
            level--;
        } else if (currentToken.t_type == Token.Type.REPEAT) {
            level++;
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
            level--;
        } else if (currentToken.t_type == Token.Type.FOR) {
            level++;
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
            level--;
        } else if (currentToken.t_type == Token.Type.LOOP) {
            level++;
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
            level--;
        } else if (currentToken.t_type == Token.Type.CASE) {
            level++;
            readToken(Token.Type.CASE);
            parseExpression();
            readToken(Token.Type.OF);
            parseCaseClauses();
            parseOtherwiseClause();
            readToken(Token.Type.END);

            buildTree("case", 3);
            level--;
        } else if (currentToken.t_type == Token.Type.READ) {
            level++;
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
            level--;
        } else if (currentToken.t_type == Token.Type.EXIT) {
            level++;
            readToken(Token.Type.EXIT);

            buildTree("exit", 0);
            level--;
        } else if (currentToken.t_type == Token.Type.RETURN) {
            level++;
            readToken(Token.Type.RETURN);
            parseExpression();

            buildTree("return", 1);
            level--;
        } else if (currentToken.t_type == Token.Type.BEGIN) {
            parseBody();
        } else {
            level++;
            buildTree("<null>", 0);
            level--;
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
            level++;
            parseExpression();

            buildTree("integer", 1);
            level--;
        } else if (currentToken.t_type == Token.Type.STRING) {
            level++;
            parseStringNode();

            buildTree("string", 1);
            level--;
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
        level++;
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
        level--;
    }

    private void parseCaseExpression() {
        parseConstValue();

        if (currentToken.t_type == Token.Type.CASE_DOTS) {
            level++;
            readToken(Token.Type.CASE_DOTS);
            parseConstValue();

            buildTree("..", 2);
            level--;
        }
    }

    private void parseOtherwiseClause() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.OTHERWISE) {
            level++;
            readToken(Token.Type.OTHERWISE);
            parseProgram();

            buildTree("otherwise", 1);
            level--;
        }
    }

    private void parseAssignment() {
        parseName();

        if (currentToken.t_type == Token.Type.ASSIGN) {
            level++;
            readToken(Token.Type.ASSIGN);
            parseExpression();

            buildTree("assign", 2);
            level--;
        } else if (currentToken.t_type == Token.Type.SWAP) {
            level++;
            readToken(Token.Type.SWAP);
            parseName();

            buildTree("swap", 2);
            level--;
        } else {
            throw new ParseError("Parse error near line: " + currentToken.line + " col: " + currentToken.col + " \nExpected: " + Token.Type.ASSIGN + " or " + Token.Type.SWAP);
        }
    }

    private void parseForStat() {
        currentToken = peek();

        if (currentToken.t_type == Token.Type.IDENTIFIER) {
            parseAssignment();
        } else {
            level++;
            buildTree("<null>", 0);
            level--;
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
            level++;
            buildTree("true", 0);
            level--;
        }
    }

    private void parseExpression() {
        parseTerm();

        if (currentToken.t_type == Token.Type.LESS_EQUAL_OP) {
            level++;
            readToken(Token.Type.GREATER_EQUAL_OP);
            parseTerm();

            buildTree("<=", 2);
            level--;
        } else if (currentToken.t_type == Token.Type.LESS_OP) {
            level++;
            readToken(Token.Type.GREATER_OP);
            parseTerm();

            buildTree("<", 2);
            level--;
        } else if (currentToken.t_type == Token.Type.GREATER_EQUAL_OP) {
            level++;
            readToken(Token.Type.GREATER_EQUAL_OP);
            parseTerm();

            buildTree(">=", 2);
            level--;
        } else if (currentToken.t_type == Token.Type.GREATER_OP) {
            level++;
            readToken(Token.Type.GREATER_OP);
            parseTerm();

            buildTree(">=", 2);
            level--;
        } else if (currentToken.t_type == Token.Type.EQUAL_OP) {
            level++;
            readToken(Token.Type.EQUAL_OP);
            parseTerm();

            buildTree("=", 2);
            level--;
        } else if (currentToken.t_type == Token.Type.NOT_EQUAL_OP) {
            level++;
            readToken(Token.Type.NOT_EQUAL_OP);
            parseTerm();

            buildTree("<>", 2);
            level--;
        }
    }

    private void parseTerm() {
        parseFactor();

        while ((currentToken.t_type == Token.Type.PLUS_OP) ||
                (currentToken.t_type == Token.Type.MINUS_OP) ||
                (currentToken.t_type == Token.Type.OR_OP)) {
            if (currentToken.t_type == Token.Type.PLUS_OP) {
                level++;
                readToken(Token.Type.PLUS_OP);
                parseFactor();

                buildTree("+", 2);
                level--;
            } else if (currentToken.t_type == Token.Type.MINUS_OP) {
                level++;
                readToken(Token.Type.MINUS_OP);
                parseFactor();

                buildTree("-", 2);
                level--;
            } else if (currentToken.t_type == Token.Type.OR_OP) {
                level++;
                readToken(Token.Type.OR_OP);
                parseFactor();

                buildTree("or", 2);
                level--;
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
                level++;
                readToken(Token.Type.MULTIPLY_OP);
                parsePrimary();

                buildTree("*", 2);
                level--;
            } else if (currentToken.t_type == Token.Type.DIVIDE_OP) {
                level++;
                readToken(Token.Type.DIVIDE_OP);
                parsePrimary();

                buildTree("/", 2);
                level--;
            } else if (currentToken.t_type == Token.Type.AND_OP) {
                level++;
                readToken(Token.Type.AND_OP);
                parsePrimary();

                buildTree("and", 2);
                level--;
            } else if (currentToken.t_type == Token.Type.MOD_OP) {
                level++;
                readToken(Token.Type.MOD_OP);
                parsePrimary();

                buildTree("mod", 2);
                level--;
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
                level++;
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
                level--;
            }
        } else if (currentToken.t_type == Token.Type.MINUS_OP) {
            level++;
            readToken(Token.Type.MINUS_OP);
            parsePrimary();

            buildTree("-", 1);
            level--;
        } else if (currentToken.t_type == Token.Type.PLUS_OP) {
            readToken(Token.Type.PLUS_OP);
            parsePrimary();
        } else if (currentToken.t_type == Token.Type.NOT_OP) {
            level++;
            readToken(Token.Type.NOT_OP);
            parsePrimary();

            buildTree("not", 1);
            level--;
        } else if (currentToken.t_type == Token.Type.EOF) {
            level++;
            readToken(Token.Type.EOF);

            buildTree("eof", 0);
            level--;
        } else if (currentToken.t_type == Token.Type.INTEGER) {
            readToken(Token.Type.INTEGER);
        } else if (currentToken.t_type == Token.Type.CHAR) {
            readToken(Token.Type.CHAR);
        } else if (currentToken.t_type == Token.Type.LPAREN) {
            readToken(Token.Type.LPAREN);
            parseExpression();
            readToken(Token.Type.RPAREN);
        } else if (currentToken.t_type == Token.Type.SUCC) {
            level++;
            readToken(Token.Type.SUCC);
            readToken(Token.Type.LPAREN);
            parseExpression();
            readToken(Token.Type.RPAREN);

            buildTree("succ", 1);
            level--;
        } else if (currentToken.t_type == Token.Type.PRED) {
            level++;
            readToken(Token.Type.PRED);
            readToken(Token.Type.LPAREN);
            parseExpression();
            readToken(Token.Type.RPAREN);

            buildTree("pred", 1);
            level--;
        } else if (currentToken.t_type == Token.Type.CHR) {
            level++;
            readToken(Token.Type.CHR);
            readToken(Token.Type.LPAREN);
            parseExpression();
            readToken(Token.Type.RPAREN);

            buildTree("chr", 1);
            level--;
        } else if (currentToken.t_type == Token.Type.ORD) {
            level++;
            readToken(Token.Type.ORD);
            readToken(Token.Type.LPAREN);
            parseExpression();
            readToken(Token.Type.RPAREN);

            buildTree("ord", 1);
            level--;
        }
    }

    private void parseName() {
        readToken(Token.Type.IDENTIFIER);
    }

    private void parseIdentifier() {

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
                level++;
                level++;
                buildTree(currentToken.text, 0);
                level--;
                buildTree("<identifier>", 1);
                level--;
            } else if (currentToken.t_type == Token.Type.STRING) {
                level++;
                level++;
                buildTree(currentToken.text, 0);
                level--;
                buildTree("<string>", 1);
                level--;
            } else if (currentToken.t_type == Token.Type.INTEGER) {
                level++;
                level++;
                buildTree(currentToken.text, 0);
                level--;
                buildTree("<integer>", 1);
                level--;
            } else if (currentToken.t_type == Token.Type.CHAR) {
                level++;
                level++;
                buildTree(currentToken.text, 0);
                level--;
                buildTree("<char>", 1);
                level--;
            }

            inputIndex++;
            currentToken = peek();
        } else {
            throw new ParseError("Parse error near line: " + currentToken.line + " col: " + currentToken.col + " \nExpected " + expType);
        }
    }

    private void buildTree(String ruleName, int n) {
        ASTNode node = new ASTNode(ruleName, n, level);

        for (int i = 0; i < n; i++) {
            ASTNode childNode = stack.pop();
            childNode.setParent(node);

            node.addChild(childNode);
        }

        node.reverseChildren();
        stack.push(node);
    }
}
