package com.yumantha;

import com.yumantha.errors.ParseError;

import java.util.ArrayList;

public class Parser {
    private ArrayList<Token> input;
    private int inputIndex;
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

    }

    public void parseConsts() {

    }

    public void parseConst() {

    }

    public void constValue() {

    }

    public void parseTypes() {

    }

    public void parseType() {

    }

    public void parseLitList() {

    }

    public void parseSubProgs() {

    }

    public void parseFcn() {

    }

    public void parseParams() {

    }

    public void parseDclns() {

    }

    public void parseDcln() {

    }

    public void parseBody() {

    }

    public void parseStatement() {

    }

    public void parseOutExp() {

    }

    public void parseStringNode() {

    }

    public void parseCaseClauses() {

    }

    public void parseCaseClause() {

    }

    public void parseCaseExpression() {

    }

    public void parseOtherwiseClause() {

    }

    public void parseAssignment() {

    }

    public void parseForStat() {

    }

    public void parseForExp() {

    }

    public void parseExpression() {

    }

    public void parseTerm() {

    }

    public void parseFactor() {

    }

    public void parsePrimary() {

    }

    public void parseName() {

    }

    private Token peekAtOffset(int offset){
        if (inputIndex + offset < input.size()){
            return input.get(inputIndex + offset);
        }

        return eof;
    }

    private Token peek(){
        return peekAtOffset(0);
    }

    private void readToken(Token.Type expType) {
        Token actualToken = peek();

        if(actualToken.t_type == expType) {
            inputIndex++;
        } else {
            throw new ParseError("Parse error near line: " + actualToken.line + " col: " + actualToken.col + " \nExpected " + expType);
        }
    }
}
