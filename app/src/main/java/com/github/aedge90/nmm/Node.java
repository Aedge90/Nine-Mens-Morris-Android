package com.github.aedge90.nmm;

import java.util.ArrayList;

public class Node {
    private ArrayList<Node> children = new ArrayList<>();
    private Node parent = null;
    private Move data = null;

    public Node(Move data) {
        this.data = data;
    }

    public Node(Move data, Node parent) {
        this.data = data;
        this.parent = parent;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public void setParent(Node parent) {
        parent.addChild(this);
        this.parent = parent;
    }

    public void addChild(Move data) {
        Node child = new Node(data);
        child.setParent(this);
        this.children.add(child);
    }

    public void addChild(Node child) {
        child.setParent(this);
        this.children.add(child);
    }

    public Move getData() {
        return this.data;
    }

    public void setData(Move data) {
        this.data = data;
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
