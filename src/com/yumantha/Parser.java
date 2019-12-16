package com.yumantha;

import com.yumantha.errors.ParseError;

import java.util.ArrayList;
import java.util.Stack;

public class Parser {
    public static ASTNode parseWinzig(ArrayList<Token> input) {
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
        currentToken = checkFirst();

        if (currentToken.t_type == Token.Type.PROG) {
            int n = 0;

            readToken(Token.Type.PROG);
            n += parseName();
            readToken(Token.Type.COLON);
            n += parseConsts();
            n += parseTypes();
            n += parseDclns();
            n += parseSubProgs();
            n += parseBody();
            n += parseName();
            readToken(Token.Type.DOT);

            buildTree("program", n);
        } else {
            throw new ParseError("Parse error near line: " + currentToken.line + " col: " + currentToken.col + " \nExpected: " + Token.Type.PROG);
        }
    }

    private int parseConsts() {
        if (currentToken.t_type == Token.Type.CONST) {
            int n = 0;

            readToken(Token.Type.CONST);
            n += parseConst();

            while (currentToken.t_type == Token.Type.COMMA) {
                readToken(Token.Type.COMMA);
                n += parseConst();
            }

            readToken(Token.Type.SEMI_COLON);

            buildTree("consts", n);
        } else {
            int n = 0;

            buildTree("consts", n);
        }
        return 1;
    }

    private int parseConst() {
        int n = 0;

        n += parseName();
        readToken(Token.Type.EQUAL_OP);
        n += parseConstValue();

        buildTree("const", n);
        return 1;
    }

    private int parseConstValue() {
        if (currentToken.t_type == Token.Type.INTEGER) {
            readToken(Token.Type.INTEGER);
            return 1;
        } else if (currentToken.t_type == Token.Type.CHAR) {
            readToken(Token.Type.CHAR);
            return 1;
        } else if (currentToken.t_type == Token.Type.IDENTIFIER) {
            return parseName();
        } else {
            throw new ParseError("Parse error near line: " + currentToken.line + " col: " + currentToken.col + " \nExpected: " + Token.Type.INTEGER + ", " + Token.Type.CHAR + " or " + Token.Type.IDENTIFIER);
        }
    }

    private int parseTypes() {
        if (currentToken.t_type == Token.Type.TYPE) {
            int n = 0;

            readToken(Token.Type.TYPE);
            n += parseType();
            readToken(Token.Type.SEMI_COLON);

            while (currentToken.t_type == Token.Type.IDENTIFIER) {
                n += parseType();
                readToken(Token.Type.SEMI_COLON);
            }

            buildTree("types", n);
        } else {
            int n = 0;
            buildTree("types", n);
        }

        return 1;
    }

    private int parseType() {
        int n = 0;

        n += parseName();
        readToken(Token.Type.EQUAL_OP);
        n += parseLitList();

        buildTree("type", n);
        return 1;
    }

    private int parseLitList() {
        int n = 0;

        readToken(Token.Type.LPAREN);
        n += parseName();

        while (currentToken.t_type == Token.Type.COMMA) {
            readToken(Token.Type.COMMA);
            n += parseName();
        }

        readToken(Token.Type.RPAREN);

        buildTree("lit", n);
        return 1;
    }

    private int parseSubProgs() {
        int n = 0;

        while (currentToken.t_type == Token.Type.FUNCTION) {
            n += parseFcn();
        }

        buildTree("subprogs", n);
        return 1;
    }

    private int parseFcn() {
        int n = 0;

        readToken(Token.Type.FUNCTION);
        n += parseName();
        readToken(Token.Type.LPAREN);
        n += parseParams();
        readToken(Token.Type.RPAREN);
        readToken(Token.Type.COLON);
        n += parseName();
        readToken(Token.Type.SEMI_COLON);
        n += parseConsts();
        n += parseTypes();
        n += parseDclns();
        n += parseBody();
        n += parseName();
        readToken(Token.Type.SEMI_COLON);

        buildTree("fcn", n);
        return 1;
    }

    private int parseParams() {
        int n = 0;

        n += parseDcln();

        while (currentToken.t_type == Token.Type.SEMI_COLON) {
            readToken(Token.Type.SEMI_COLON);
            n += parseDcln();
        }

        buildTree("params", n);
        return 1;
    }

    private int parseDclns() {
        if (currentToken.t_type == Token.Type.VAR) {
            int n = 0;

            readToken(Token.Type.VAR);
            n += parseDcln();
            readToken(Token.Type.SEMI_COLON);

            while (currentToken.t_type == Token.Type.IDENTIFIER) {
                n += parseDcln();
                readToken(Token.Type.SEMI_COLON);
            }

            buildTree("dclns", n);
        } else {
            int n = 0;

            buildTree("dclns", n);
        }

        return 1;
    }

    private int parseDcln() {
        int n = 0;

        n += parseName();

        while (currentToken.t_type == Token.Type.COMMA) {
            readToken(Token.Type.COMMA);
            n += parseName();
        }

        readToken(Token.Type.COLON);
        n += parseName();

        buildTree("var", n);
        return 1;
    }

    private int parseBody() {
        int n = 0;

        readToken(Token.Type.BEGIN);
        n += parseStatement();

        while (currentToken.t_type == Token.Type.SEMI_COLON) {
            readToken(Token.Type.SEMI_COLON);
            n += parseStatement();
        }

        readToken(Token.Type.END);

        buildTree("block", n);
        return 1;
    }

    private int parseStatement() {
        if (currentToken.t_type == Token.Type.IDENTIFIER) {
            return parseAssignment();
        } else if (currentToken.t_type == Token.Type.OUTPUT) {
            int n = 0;

            readToken(Token.Type.OUTPUT);
            readToken(Token.Type.LPAREN);
            n += parseOutExp();

            while (currentToken.t_type == Token.Type.COMMA) {
                readToken(Token.Type.COMMA);
                n += parseOutExp();
            }

            readToken(Token.Type.RPAREN);

            buildTree("output", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.IF) {
            int n = 0;

            readToken(Token.Type.IF);
            n += parseExpression();
            readToken(Token.Type.THEN);
            n += parseStatement();

            if (currentToken.t_type == Token.Type.ELSE) {
                readToken(Token.Type.ELSE);
                n += parseStatement();
            }

            buildTree("if", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.WHILE) {
            int n = 0;

            readToken(Token.Type.WHILE);
            n += parseExpression();
            readToken(Token.Type.DO);
            n += parseStatement();

            buildTree("while", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.REPEAT) {
            int n = 0;

            readToken(Token.Type.REPEAT);
            n += parseStatement();

            while (currentToken.t_type == Token.Type.SEMI_COLON) {
                readToken(Token.Type.SEMI_COLON);
                n += parseStatement();
            }

            readToken(Token.Type.UNTIL);
            n += parseExpression();

            buildTree("repeat", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.FOR) {
            int n = 0;

            readToken(Token.Type.FOR);
            readToken(Token.Type.LPAREN);
            n += parseForStat();
            readToken(Token.Type.SEMI_COLON);
            n += parseForExp();
            readToken(Token.Type.SEMI_COLON);
            n += parseForStat();
            readToken(Token.Type.RPAREN);
            n += parseStatement();

            buildTree("for", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.LOOP) {
            int n = 0;

            readToken(Token.Type.LOOP);
            n += parseStatement();

            while (currentToken.t_type == Token.Type.SEMI_COLON) {
                readToken(Token.Type.SEMI_COLON);
                n += parseStatement();
            }

            readToken(Token.Type.POOL);

            buildTree("loop", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.CASE) {
            int n = 0;

            readToken(Token.Type.CASE);
            n += parseExpression();
            readToken(Token.Type.OF);
            n += parseCaseClauses();
            n += parseOtherwiseClause();
            readToken(Token.Type.END);

            buildTree("case", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.READ) {
            int n = 0;

            readToken(Token.Type.READ);
            readToken(Token.Type.LPAREN);
            n += parseName();

            while (currentToken.t_type == Token.Type.COMMA) {
                readToken(Token.Type.COMMA);
                n += parseName();
            }

            readToken(Token.Type.RPAREN);

            buildTree("read", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.EXIT) {
            int n = 0;

            readToken(Token.Type.EXIT);

            buildTree("exit", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.RETURN) {
            int n = 0;

            readToken(Token.Type.RETURN);
            n += parseExpression();

            buildTree("return", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.BEGIN) {
            return parseBody();
        } else {
            int n = 0;
            buildTree("<null>", n);
            return 1;
        }
    }

    private int parseOutExp() {
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
            int n = 0;

            n += parseExpression();

            buildTree("integer", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.STRING) {
            int n = 0;

            n += parseStringNode();

            buildTree("string", n);
            return 1;
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

    private int parseStringNode() {
        readToken(Token.Type.STRING);
        return 1;
    }

    private int parseCaseClauses() {
        int n = 0;

        n += parseCaseClause();
        readToken(Token.Type.SEMI_COLON);

        while ((currentToken.t_type == Token.Type.INTEGER) ||
                (currentToken.t_type == Token.Type.CHAR) ||
                (currentToken.t_type == Token.Type.IDENTIFIER)) {
            n += parseCaseClause();
            readToken(Token.Type.SEMI_COLON);
        }

        return n;
    }

    private int parseCaseClause() {
        int n = 0;

        n += parseCaseExpression();

        while (currentToken.t_type == Token.Type.COMMA) {
            readToken(Token.Type.COMMA);
            n += parseCaseExpression();
        }

        readToken(Token.Type.COLON);
        n += parseStatement();

        buildTree("case_clause", n);
        return 1;
    }

    private int parseCaseExpression() {
        int n = 0;

        n += parseConstValue();

        if (currentToken.t_type == Token.Type.CASE_DOTS) {
            readToken(Token.Type.CASE_DOTS);
            n += parseConstValue();

            buildTree("..", n);
            return 1;
        }

        return n;
    }

    private int parseOtherwiseClause() {
        if (currentToken.t_type == Token.Type.OTHERWISE) {
            int n = 0;

            readToken(Token.Type.OTHERWISE);
            n += parseStatement();

            buildTree("otherwise", n);
            return 1;
        }

        return 0;
    }

    private int parseAssignment() {
        int n = 0;

        n += parseName();

        if (currentToken.t_type == Token.Type.ASSIGN) {
            readToken(Token.Type.ASSIGN);
            n += parseExpression();

            buildTree("assign", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.SWAP) {
            readToken(Token.Type.SWAP);
            n += parseName();

            buildTree("swap", n);
            return 1;
        } else {
            throw new ParseError("Parse error near line: " + currentToken.line + " col: " + currentToken.col + " \nExpected: " + Token.Type.ASSIGN + " or " + Token.Type.SWAP);
        }
    }

    private int parseForStat() {
        if (currentToken.t_type == Token.Type.IDENTIFIER) {
            return parseAssignment();
        } else {
            int n = 0;

            buildTree("<null>", n);
            return 1;
        }
    }

    private int parseForExp() {
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
            return parseExpression();
        } else {
            int n = 0;

            buildTree("true", n);
            return 1;
        }
    }

    private int parseExpression() {
        int n = 0;

        n += parseTerm();

        if (currentToken.t_type == Token.Type.LESS_EQUAL_OP) {
            readToken(Token.Type.LESS_EQUAL_OP);
            n += parseTerm();

            buildTree("<=", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.LESS_OP) {
            readToken(Token.Type.LESS_OP);
            n += parseTerm();

            buildTree("<", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.GREATER_EQUAL_OP) {
            readToken(Token.Type.GREATER_EQUAL_OP);
            n += parseTerm();

            buildTree(">=", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.GREATER_OP) {
            readToken(Token.Type.GREATER_OP);
            n += parseTerm();

            buildTree(">", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.EQUAL_OP) {
            readToken(Token.Type.EQUAL_OP);
            n += parseTerm();

            buildTree("=", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.NOT_EQUAL_OP) {
            readToken(Token.Type.NOT_EQUAL_OP);
            n += parseTerm();

            buildTree("<>", n);
            return 1;
        }

        return n;
    }

    private int parseTerm() {
        int n = 0;

        n += parseFactor();

        if ((currentToken.t_type == Token.Type.PLUS_OP) ||
                (currentToken.t_type == Token.Type.MINUS_OP) ||
                (currentToken.t_type == Token.Type.OR_OP)) {
            while ((currentToken.t_type == Token.Type.PLUS_OP) ||
                    (currentToken.t_type == Token.Type.MINUS_OP) ||
                    (currentToken.t_type == Token.Type.OR_OP)) {
                parseTermInternal(n);
            }
            return 1;
        } else {
            return n;
        }
    }

    private void parseTermInternal(int n) {
        if (currentToken.t_type == Token.Type.PLUS_OP) {
            readToken(Token.Type.PLUS_OP);
            n += parseFactor();

            buildTree("+", n);
        } else if (currentToken.t_type == Token.Type.MINUS_OP) {
            readToken(Token.Type.MINUS_OP);
            n += parseFactor();

            buildTree("-", n);
        } else if (currentToken.t_type == Token.Type.OR_OP) {
            readToken(Token.Type.OR_OP);
            n += parseFactor();

            buildTree("or", n);
        }
    }

    private int parseFactor() {
        int n = 0;

        n += parsePrimary();

        if ((currentToken.t_type == Token.Type.MULTIPLY_OP) ||
                (currentToken.t_type == Token.Type.DIVIDE_OP) ||
                (currentToken.t_type == Token.Type.AND_OP) ||
                (currentToken.t_type == Token.Type.MOD_OP)) {
            while ((currentToken.t_type == Token.Type.MULTIPLY_OP) ||
                    (currentToken.t_type == Token.Type.DIVIDE_OP) ||
                    (currentToken.t_type == Token.Type.AND_OP) ||
                    (currentToken.t_type == Token.Type.MOD_OP)) {
                parseFactorInternal(n);
            }
            return 1;
        } else {
            return n;
        }
    }

    private void parseFactorInternal(int n) {
        if (currentToken.t_type == Token.Type.MULTIPLY_OP) {
            readToken(Token.Type.MULTIPLY_OP);
            n += parsePrimary();

            buildTree("*", n);
        } else if (currentToken.t_type == Token.Type.DIVIDE_OP) {
            readToken(Token.Type.DIVIDE_OP);
            n += parsePrimary();

            buildTree("/", n);
        } else if (currentToken.t_type == Token.Type.AND_OP) {
            readToken(Token.Type.AND_OP);
            n += parsePrimary();

            buildTree("and", n);
        } else if (currentToken.t_type == Token.Type.MOD_OP) {
            readToken(Token.Type.MOD_OP);
            n += parsePrimary();

            buildTree("mod", n);
        }
    }

    private int parsePrimary() {
        if (currentToken.t_type == Token.Type.IDENTIFIER) {
            int n = 0;

            n += parseName();

            if (currentToken.t_type == Token.Type.LPAREN) {
                readToken(Token.Type.LPAREN);
                n += parseExpression();

                while (currentToken.t_type == Token.Type.COMMA) {
                    readToken(Token.Type.COMMA);
                    n += parseExpression();
                }

                readToken(Token.Type.RPAREN);

                buildTree("call", n);
                return 1;
            }
            return n;
        } else if (currentToken.t_type == Token.Type.MINUS_OP) {
            int n = 0;

            readToken(Token.Type.MINUS_OP);
            n += parsePrimary();

            buildTree("-", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.PLUS_OP) {
            readToken(Token.Type.PLUS_OP);
            return parsePrimary();
        } else if (currentToken.t_type == Token.Type.NOT_OP) {
            int n = 0;

            readToken(Token.Type.NOT_OP);
            n += parsePrimary();

            buildTree("not", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.EOF) {
            int n = 0;

            readToken(Token.Type.EOF);

            buildTree("eof", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.INTEGER) {
            readToken(Token.Type.INTEGER);
            return 1;
        } else if (currentToken.t_type == Token.Type.CHAR) {
            readToken(Token.Type.CHAR);
            return 1;
        } else if (currentToken.t_type == Token.Type.LPAREN) {
            int n = 0;

            readToken(Token.Type.LPAREN);
            n += parseExpression();
            readToken(Token.Type.RPAREN);
            return n;
        } else if (currentToken.t_type == Token.Type.SUCC) {
            int n = 0;

            readToken(Token.Type.SUCC);
            readToken(Token.Type.LPAREN);
            n += parseExpression();
            readToken(Token.Type.RPAREN);

            buildTree("succ", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.PRED) {
            int n = 0;

            readToken(Token.Type.PRED);
            readToken(Token.Type.LPAREN);
            n += parseExpression();
            readToken(Token.Type.RPAREN);

            buildTree("pred", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.CHR) {
            int n = 0;

            readToken(Token.Type.CHR);
            readToken(Token.Type.LPAREN);
            n += parseExpression();
            readToken(Token.Type.RPAREN);

            buildTree("chr", n);
            return 1;
        } else if (currentToken.t_type == Token.Type.ORD) {
            int n = 0;

            readToken(Token.Type.ORD);
            readToken(Token.Type.LPAREN);
            n += parseExpression();
            readToken(Token.Type.RPAREN);

            buildTree("ord", n);
            return 1;
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
                    + Token.Type.CHR + " or "
                    + Token.Type.ORD
            );
        }
    }

    private int parseName() {
        readToken(Token.Type.IDENTIFIER);
        return 1;
    }

    private Token checkFirst() {
        if (inputIndex < input.size()) {
            return input.get(inputIndex);
        }

        return eof;
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
            currentToken = checkFirst();
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
