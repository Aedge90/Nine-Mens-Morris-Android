package com.github.aedge90.nmm;

import java.util.List;

public class MoveNode {
    //just use an array instead of datastructures to save memory and performance
    private MoveNode[] children = null;
    private MoveNode parent = null;
    private Move data = null;

    public MoveNode(Move move) {
        this.data = move;
    }

    public MoveNode(Move move, MoveNode parent) {
        this.data = move;
        this.parent = parent;
    }

    public MoveNode[] getChildren() {
        return children;
    }

    private void setParent(MoveNode parent) {
        this.parent = parent;
    }

    public void addChildren (List<Move> newChildren){
        //this may also be 0, but luckily java allows arrays of zero size. so no child is added then
        children = new MoveNode[newChildren.size()];
        int index = 0;
        for (Move child : newChildren) {
            MoveNode childNode = new MoveNode(child);
            childNode.setParent(this);
            children[index] = childNode;
            index++;
        }
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

    public void removeParent() {
        this.parent = null;
    }
}
