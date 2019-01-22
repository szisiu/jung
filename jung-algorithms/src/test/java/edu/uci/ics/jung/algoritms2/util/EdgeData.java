package edu.uci.ics.jung.algoritms2.util;

public final class EdgeData {

    private Double card;
    private Double freq;
    private int id;

    public EdgeData(int id, Double freq, Double card) {
        this.id = id;
        if (freq < 1.0) {
            throw new IllegalArgumentException("cannot add an edge with a freq < 0");
        }
        this.freq = freq;
        if (card < 2.0) {
            throw new IllegalArgumentException("cannot add an edge with a card < 1");
        }
        this.card = card;
    }

    public Double getCard() {
        return card;
    }

    public void setCard(Double card) {
        this.card = card;
    }

    public Double getFreq() {
        return freq;
    }

    public void setFreq(Double freq) {
        this.freq = freq;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toString() {
        return "E" + id;
    }
}