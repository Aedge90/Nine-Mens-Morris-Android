package com.github.aedge90.nmm;

import java.util.List;

public class MoveNode extends Move{

    //just use an array instead of datastructures to save memory and performance
    private MoveNode[] children = null;

    public MoveNode(Position dest, Position src, Position kill) {
        super(dest, src, kill);
    }

    public MoveNode[] getChildren() {
        return children;
    }

    public void addChildren (List<Move> newChildren){
        //this may also be 0, but luckily java allows arrays of zero size. so no child is added then
        children = new MoveNode[newChildren.size()];
        int index = 0;
        for (Move child : newChildren) {
            MoveNode childNode = new MoveNode(child.getDest() , child.getSrc() , child.getKill());
            children[index] = childNode;
            index++;
        }
    }

    public void removeChildren(){
        children = null;
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
