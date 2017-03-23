package com.github.aedge90.nmm;

import java.util.ArrayList;

public class MoveNode {
    private ArrayList<MoveNode> children = new ArrayList<>();
    private MoveNode parent = null;
    private Move data = null;

    public MoveNode(Move move) {
        this.data = move;
    }

    public MoveNode(Move move, MoveNode parent) {
        this.data = move;
        this.parent = parent;
    }

    public ArrayList<MoveNode> getChildren() {
        return children;
    }

    private void setParent(MoveNode parent) {
        this.parent = parent;
    }

    public void addChild(MoveNode child) {
        child.setParent(this);
        this.children.add(child);
    }

    public void addChild(Move data) {
        MoveNode newChild = new MoveNode(data);
        newChild.setParent(this);
        children.add(newChild);
    }

    public Move getMove() {
        return this.data;
    }

    public void setMove(Move move) {
        this.data = move;
    }

    public boolean isRoot() {
        return (this.parent == null);
    }

    public boolean isLeaf() {
        if(this.children.size() == 0)
            return true;
        else
            return false;
    }

    public void removeParent() {
        this.parent = null;
    }
}
