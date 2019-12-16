package com.yumantha;

import com.yumantha.errors.ScannerError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yumantha.Token.Type;

public class Scanner {
    public static ArrayList<Token> scan(String input) {
        Scanner scanner = new Scanner(input);
        scanner.scan();

        return scanner.result;
    }

    private static final Pattern identifierPattern = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
    private static final Pattern integerPattern = Pattern.compile("[0-9]+");
    private static final Pattern charPattern = Pattern.compile("'[^']'");
    private static final Pattern stringPattern = Pattern.compile("\"([^\"]*)\"");
    private static final Pattern multiCommentPattern = Pattern.compile("\\{(.*?)}", Pattern.DOTALL);
    private static final Pattern singleCommentPattern = Pattern.compile("#(.*)?");
    private static final Pattern whitespacePattern = Pattern.compile("[\\u0020\\u000c\\u0009\\u000b\\u000a]");                 // space, form feed, horizontal tab, vertical tab, line feed (new line)

    private static final HashMap<String, Type> keywords = new HashMap<String, Type>();

    static {
//        keywords.put("\n", Type.NEWLINE);
        keywords.put("program", Type.PROG);
        keywords.put("var", Type.VAR);
        keywords.put("const", Type.CONST);
        keywords.put("type", Type.TYPE);
        keywords.put("function", Type.FUNCTION);
        keywords.put("return", Type.RETURN);
        keywords.put("begin", Type.BEGIN);
        keywords.put("end", Type.END);
        keywords.put(":=:", Type.SWAP);
        keywords.put(":=", Type.ASSIGN);
        keywords.put("output", Type.OUTPUT);
        keywords.put("if", Type.IF);
        keywords.put("then", Type.THEN);
        keywords.put("else", Type.ELSE);
        keywords.put("while", Type.WHILE);
        keywords.put("do", Type.DO);
        keywords.put("case", Type.CASE);
        keywords.put("of", Type.OF);
        keywords.put("..", Type.CASE_DOTS);
        keywords.put("otherwise", Type.OTHERWISE);
        keywords.put("repeat", Type.REPEAT);
        keywords.put("for", Type.FOR);
        keywords.put("until", Type.UNTIL);
        keywords.put("loop", Type.LOOP);
        keywords.put("pool", Type.POOL);
        keywords.put("exit", Type.EXIT);
        keywords.put("<=", Type.LESS_EQUAL_OP);
        keywords.put("<>", Type.NOT_EQUAL_OP);
        keywords.put("<", Type.LESS_OP);
        keywords.put(">=", Type.GREATER_EQUAL_OP);
        keywords.put(">", Type.GREATER_OP);
        keywords.put("=", Type.EQUAL_OP);
        keywords.put("mod", Type.MOD_OP);
        keywords.put("and", Type.AND_OP);
        keywords.put("or", Type.OR_OP);
        keywords.put("not", Type.NOT_OP);
        keywords.put("read", Type.READ);
        keywords.put("succ", Type.SUCC);
        keywords.put("pred", Type.PRED);
        keywords.put("chr", Type.CHR);
        keywords.put("ord", Type.ORD);
        keywords.put("eof", Type.EOF);
        keywords.put("{", Type.BLOCK_BEGIN);
        keywords.put(":", Type.COLON);
        keywords.put(";", Type.SEMI_COLON);
        keywords.put(".", Type.DOT);
        keywords.put(",", Type.COMMA);
        keywords.put("(", Type.LPAREN);
        keywords.put(")", Type.RPAREN);
        keywords.put("+", Type.PLUS_OP);
        keywords.put("-", Type.MINUS_OP);
        keywords.put("*", Type.MULTIPLY_OP);
        keywords.put("/", Type.DIVIDE_OP);
    }

    private ArrayList<Token> result;
    private String input;
    private int line;
    private int col;

    private Scanner(String input) {
        this.result = new ArrayList<Token>();
        this.input = input;
        this.line = 1;
        this.col = 1;
    }

    private void scan() {
        skipWhitespacesAndComments();

        while (!input.isEmpty()) {
            boolean isValidToken =
//                    checkToken("\n", Type.NEWLINE) ||
                    checkKeywordIdentifier() ||
                            checkToken("program", Type.PROG) ||
                            checkToken("var", Type.VAR) ||
                            checkToken("const", Type.CONST) ||
                            checkToken("type", Type.TYPE) ||
                            checkToken("function", Type.FUNCTION) ||
                            checkToken("return", Type.RETURN) ||
                            checkToken("begin", Type.BEGIN) ||
                            checkToken("end", Type.END) ||
                            checkToken(":=:", Type.SWAP) ||
                            checkToken(":=", Type.ASSIGN) ||
                            checkToken("output", Type.OUTPUT) ||
                            checkToken("if", Type.IF) ||
                            checkToken("then", Type.THEN) ||
                            checkToken("else", Type.ELSE) ||
                            checkToken("while", Type.WHILE) ||
                            checkToken("do", Type.DO) ||
                            checkToken("case", Type.CASE) ||
                            checkToken("of", Type.OF) ||
                            checkToken("..", Type.CASE_DOTS) ||
                            checkToken("otherwise", Type.OTHERWISE) ||
                            checkToken("repeat", Type.REPEAT) ||
                            checkToken("for", Type.FOR) ||
                            checkToken("until", Type.UNTIL) ||
                            checkToken("loop", Type.LOOP) ||
                            checkToken("pool", Type.POOL) ||
                            checkToken("exit", Type.EXIT) ||
                            checkToken("<=", Type.LESS_EQUAL_OP) ||
                            checkToken("<>", Type.NOT_EQUAL_OP) ||
                            checkToken("<", Type.LESS_OP) ||
                            checkToken(">=", Type.GREATER_EQUAL_OP) ||
                            checkToken(">", Type.GREATER_OP) ||
                            checkToken("=", Type.EQUAL_OP) ||
                            checkToken("mod", Type.MOD_OP) ||
                            checkToken("and", Type.AND_OP) ||
                            checkToken("or", Type.OR_OP) ||
                            checkToken("not", Type.NOT_OP) ||
                            checkToken("read", Type.READ) ||
                            checkToken("succ", Type.SUCC) ||
                            checkToken("pred", Type.PRED) ||
                            checkToken("chr", Type.CHR) ||
                            checkToken("ord", Type.ORD) ||
                            checkToken("eof", Type.EOF) ||
                            checkToken("{", Type.BLOCK_BEGIN) ||
                            checkToken(":", Type.COLON) ||
                            checkToken(";", Type.SEMI_COLON) ||
                            checkToken(".", Type.DOT) ||
                            checkToken(",", Type.COMMA) ||
                            checkToken("(", Type.LPAREN) ||
                            checkToken(")", Type.RPAREN) ||
                            checkToken("+", Type.PLUS_OP) ||
                            checkToken("-", Type.MINUS_OP) ||
                            checkToken("*", Type.MULTIPLY_OP) ||
                            checkToken("/", Type.DIVIDE_OP) ||
                            checkRegExp(integerPattern, Type.INTEGER) ||
                            checkRegExp(charPattern, Type.CHAR) ||
                            checkRegExp(stringPattern, Type.STRING);
            if (!isValidToken) {
                System.out.println("Cannot tokenize at line: " + line + " col: " + col);
                throw new ScannerError("Cannot tokenize at line: " + line + " col: " + col);
            }

            skipWhitespacesAndComments();
        }
    }

    private boolean isWhiteSpace(char character) {
        Matcher whitespaceMatcher = whitespacePattern.matcher(String.valueOf(character));

        return whitespaceMatcher.matches();
    }

    private boolean startsWithSingleComment() {
        Matcher singleCommentMatcher = singleCommentPattern.matcher(input);

        return (input.startsWith("#") && singleCommentMatcher.lookingAt());
    }

    private boolean startsWithMultiComment() {
        Matcher multiCommentMatcher = multiCommentPattern.matcher(input);

        return (input.startsWith("{") && multiCommentMatcher.lookingAt());
    }

    private boolean startsWithWhiteSpace() {
        Matcher whitespaceMatcher = whitespacePattern.matcher(input);

        return (whitespaceMatcher.lookingAt());
    }


    private void skipWhiteSpaces() {
        int i = 0;

        while (i < input.length() && isWhiteSpace(input.charAt(i))) {
            i++;
        }

        consumeInput(i);
    }

    private void skipComments() {
        Matcher singleCommentMatcher = singleCommentPattern.matcher(input);
        Matcher multiCommentMatcher = multiCommentPattern.matcher(input);

        if (input.startsWith("#")) {
            if (singleCommentMatcher.lookingAt()) {
                consumeInput(singleCommentMatcher.end());
            }
        } else if (input.startsWith("{")) {
            if (multiCommentMatcher.lookingAt()) {
                consumeInput(multiCommentMatcher.end());
            }
        }
    }

    private void skipWhitespacesAndComments() {
        while (input.length() > 0 && (startsWithWhiteSpace() || startsWithMultiComment() || startsWithSingleComment())) {
            if (startsWithWhiteSpace()) {
                skipWhiteSpaces();
            }

            if (startsWithSingleComment() || startsWithMultiComment()) {
                skipComments();
            }
        }

    }

    private boolean checkToken(String expected, Token.Type t_type) {
        if (input.startsWith(expected)) {
            result.add(new Token(t_type, expected, line, col));
            consumeInput(expected.length());
            return true;
        } else {
            return false;
        }
    }

    private boolean checkRegExp(Pattern p, Token.Type t_type) {
        Matcher m = p.matcher(input);

        if (m.lookingAt()) {
            result.add(new Token(t_type, m.group(), line, col));
            consumeInput(m.end());
            return true;
        } else {
            return false;
        }
    }

    private boolean checkKeywordIdentifier() {
        if (checkRegExp(identifierPattern, Type.IDENTIFIER)) {
            Token t = result.get(result.size() - 1);
            Token.Type t_type = keywords.get(t.text);

            if (t_type != null) {
                t = new Token(t_type, t.text, t.line, t.col);
                result.set(result.size() - 1, t);
            }

            return true;
        } else {
            return false;
        }
    }

    private void consumeInput(int amount) {
        for (int i = 0; i < amount; ++i) {
            char c = input.charAt(i);

            if (c == '\n') {
                line++;
                col = 1;
            } else {
                col++;
            }
        }

        input = input.substring(amount);
    }
}
