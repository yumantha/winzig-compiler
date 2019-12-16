package com.yumantha;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ASTNode {
    private String ruleName;
    private int n;
    private ASTNode parent;
    private ArrayList<ASTNode> children;

    public ASTNode(String ruleName, int n) {
        this.ruleName = ruleName;
        this.n = n;
        this.parent = null;
        this.children = new ArrayList<ASTNode>();
    }

    public void setParent(ASTNode parent) {
        this.parent = parent;
    }

    public ASTNode getParent() {
        return this.parent;
    }

    public void addChild(ASTNode newChild) {
        this.children.add(newChild);
    }

    public void reverseChildren() {
        Collections.reverse(this.children);
    }

    public void inOrderTraverse() {
        System.out.println(this);

        for (ASTNode node : this.children) {
            node.inOrderTraverse();
        }
    }

    public void writeTree(BufferedWriter bw) throws IOException {
        bw.write(this.toString());
        bw.write("\n");

        for (ASTNode node : this.children) {
            node.writeTree(bw);
        }
    }

    private int getLevel() {
        int level = 0;

        ASTNode checkParent = this.getParent();

        while (checkParent != null) {
            checkParent = checkParent.getParent();
            level++;
        }

        return level;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < getLevel(); i++) {
            str.append(". ");
        }

        str.append(this.ruleName).append("(").append(this.n).append(")");

        return str.toString();
    }
}
