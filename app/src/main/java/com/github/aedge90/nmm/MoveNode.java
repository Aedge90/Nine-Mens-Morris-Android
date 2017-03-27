package com.github.aedge90.nmm;

import java.util.List;

public class MoveNode {
    //just use an array instead of datastructures to save memory and performance
    private MoveNode[] children = null;
    private Move data = null;

    public MoveNode(Move move) {
        this.data = move;
    }

    public MoveNode[] getChildren() {
        return children;
    }

    public void addChildren (List<Move> newChildren){
        //this may also be 0, but luckily java allows arrays of zero size. so no child is added then
        children = new MoveNode[newChildren.size()];
        int index = 0;
        for (Move child : newChildren) {
            MoveNode childNode = new MoveNode(child);
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

    public int getDepth() {
        int deepest = 0;
        if(children != null) {
            for (MoveNode child : children) {
                deepest = Math.max(deepest, child.getDepth());
            }
        }
        return deepest + 1;
    }
}
