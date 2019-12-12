package com.yumantha;

public class Token {
    public static enum Type {
        //        NEWLINE,
        PROG,
        VAR,
        CONST,
        TYPE,
        FUNCTION,
        RETURN,
        BEGIN,
        END,
        SWAP,
        ASSIGN,
        OUTPUT,
        IF,
        THEN,
        ELSE,
        WHILE,
        DO,
        CASE,
        OF,
        CASE_DOTS,
        OTHERWISE,
        REPEAT,
        FOR,
        UNTIL,
        LOOP,
        POOL,
        EXIT,
        LESS_EQUAL_OP,
        NOT_EQUAL_OP,
        LESS_OP,
        GREATER_EQUAL_OP,
        GREATER_OP,
        EQUAL_OP,
        MOD_OP,
        AND_OP,
        OR_OP,
        NOT_OP,
        READ,
        SUCC,
        PRED,
        CHR,
        ORD,
        EOF,
        BLOCK_BEGIN,
        COLON,
        SEMI_COLON,
        DOT,
        COMMA,
        LPAREN,
        RPAREN,
        PLUS_OP,
        MINUS_OP,
        MULTIPLY_OP,
        DIVIDE_OP,
        INTEGER,
        CHAR,
        STRING,
        IDENTIFIER,
    }

    public final Type t_type;
    public final String text;
    public final int line;
    public final int col;
    public final int endCol;

    public Token(Type type, String text, int line, int col) {
        this.t_type = type;
        this.text = text;
        this.line = line;
        this.col = col;
        this.endCol = col + text.length();
    }

    @Override
    public String toString() {
//        if (t_type == Type.NEWLINE) {
//            return t_type.toString() + "(\\n)@" + line + ":" + col;
//        }

        return t_type.toString() + "(" + text + ")@" + line + ":" + col;
    }
}
