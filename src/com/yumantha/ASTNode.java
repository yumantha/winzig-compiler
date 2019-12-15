package com.yumantha;

import java.util.ArrayList;
import java.util.Collections;

public class ASTNode {
    private String ruleName;
    private int n;
    private int level;
    private ASTNode parent;
    private ArrayList<ASTNode> children;

    public ASTNode(String ruleName, int n, int level) {
        this.ruleName = ruleName;
        this.n = n;
        this.level = level;
        this.parent = null;
        this.children = new ArrayList<ASTNode>();
    }

    public void setParent(ASTNode parent) {
        this.parent = parent;
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

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        for(int i=0;i<level;i++){
            str.append(". ");
        }

        str.append(this.ruleName).append("(").append(this.n).append(")");

        return str.toString();
    }
}
