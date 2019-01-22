package edu.uci.ics.jung.algoritms2.util;

public final class VertexData {

    private int id;

    public VertexData(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toString() {
        return "V" + id;
    }
}