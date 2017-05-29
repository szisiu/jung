package edu.uci.ics.jung.algoritms2.centrality;

import com.google.common.collect.Sets;
import edu.uci.ics.jung.algorithms2.centrality.BetweennessCentrality;
import edu.uci.ics.jung.algorithms2.centrality.CentralityMode;
import edu.uci.ics.jung.algorithms2.centrality.ClosenessCentrality;
import edu.uci.ics.jung.algorithms2.centrality.DegreeCentrality;
import edu.uci.ics.jung.graph.DirectedSparseHypergraph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.UndirectedSparseHypergraph;
import org.junit.Test;

import java.util.OptionalDouble;

public class CentralityTest {

    private void calculateCentralities(Hypergraph<VertexData, EdgeData> g) {
        DegreeCentrality<VertexData, EdgeData> degreeIn = new DegreeCentrality<>(g, CentralityMode.IN);
        DegreeCentrality<VertexData, EdgeData> degreeOut = new DegreeCentrality<>(g, CentralityMode.OUT);
        DegreeCentrality<VertexData, EdgeData> degree = new DegreeCentrality<>(g, CentralityMode.TOTAL);

        DegreeCentrality<VertexData, EdgeData> degreeInW1 = new DegreeCentrality<>(g, CentralityMode.IN, e -> e.getFreq());
        DegreeCentrality<VertexData, EdgeData> degreeOutW1 = new DegreeCentrality<>(g, CentralityMode.OUT, e -> e.getFreq());
        DegreeCentrality<VertexData, EdgeData> degreeW1 = new DegreeCentrality<>(g, CentralityMode.TOTAL, e -> e.getFreq());

        DegreeCentrality<VertexData, EdgeData> degreeInW2 = new DegreeCentrality<>(g, CentralityMode.IN, e -> e.getFreq() / (e.getCard() - 1));
        DegreeCentrality<VertexData, EdgeData> degreeOutW2 = new DegreeCentrality<>(g, CentralityMode.OUT, e -> e.getFreq() / (e.getCard() - 1));
        DegreeCentrality<VertexData, EdgeData> degreeW2 = new DegreeCentrality<>(g, CentralityMode.TOTAL, e -> e.getFreq() / (e.getCard() - 1));

        OptionalDouble freqAvg = g.getEdges().stream().mapToDouble(e -> e.getFreq()).average();
        OptionalDouble maxAvg = g.getEdges().stream().mapToDouble(e -> e.getFreq()).max();

        ClosenessCentrality<VertexData, EdgeData> closeness = new ClosenessCentrality<>(g, false, false);
        ClosenessCentrality<VertexData, EdgeData> closenessW1 = new ClosenessCentrality<>(g, e -> freqAvg.getAsDouble() / e.getFreq(), false, false);
        ClosenessCentrality<VertexData, EdgeData> closenessW2 = new ClosenessCentrality<>(g, e -> (e.getCard() - 1)*e.getFreq() / freqAvg.getAsDouble() , false, false);

        BetweennessCentrality<VertexData, EdgeData> betweenness = new BetweennessCentrality<>(g, false);
        BetweennessCentrality<VertexData, EdgeData> betweennessW1 = new BetweennessCentrality<>(g, e -> freqAvg.getAsDouble() / e.getFreq(), false);
        BetweennessCentrality<VertexData, EdgeData> betweennessW2 = new BetweennessCentrality<>(g, e -> (e.getCard() - 1)*e.getFreq() / freqAvg.getAsDouble(), false);


        System.out.println("degree (unweighted)");
        g.getVertices().forEach(v -> System.out.println(v + " | " + degreeIn.getVertexScore(v) + " | " + degreeOut.getVertexScore(v) + " | " + degree.getVertexScore(v)));

        System.out.println("degree (strength)");
        g.getVertices().forEach(v -> System.out.println(v + " | " + degreeInW1.getVertexScore(v) + " | " + degreeOutW1.getVertexScore(v) + " | " + degreeW1.getVertexScore(v)));

        System.out.println("degree (hyperstrength)");
        g.getVertices().forEach(v -> System.out.println(v + " | " + degreeInW2.getVertexScore(v) + " | " + degreeOutW2.getVertexScore(v) + " | " + degreeW2.getVertexScore(v)));

        System.out.println("closeness (unweighted)");
        g.getVertices().forEach(v -> System.out.println(v + " | " + closeness.getVertexScore(v)));

        System.out.println("closeness (strength)");
        g.getVertices().forEach(v -> System.out.println(v + " | " + closenessW1.getVertexScore(v)));

        System.out.println("closeness (hyperstrength)");
        g.getVertices().forEach(v -> System.out.println(v + " | " + closenessW2.getVertexScore(v)));

        System.out.println("betweenness (unweighted)");
        g.getVertices().forEach(v -> System.out.println(v + " | " + betweenness.getVertexScore(v)));

        System.out.println("betweenness (strength)");
        g.getVertices().forEach(v -> System.out.println(v + " | " + betweennessW1.getVertexScore(v)));

        System.out.println("betweenness (hyperstrength)");
        g.getVertices().forEach(v -> System.out.println(v + " | " + betweennessW2.getVertexScore(v)));
    }

    @Test
    public void directedHypergraphCentrality() {
        VertexData v1 = new VertexData(1);
        VertexData v2 = new VertexData(2);
        VertexData v3 = new VertexData(3);
        VertexData v4 = new VertexData(4);

        EdgeData e1 = new EdgeData(1, 4.0, 3.0);
        EdgeData e2 = new EdgeData(2, 1.0, 2.0);
        EdgeData e3 = new EdgeData(3, 1.0, 2.0);
        EdgeData e4 = new EdgeData(4, 4.0, 2.0);

        DirectedSparseHypergraph<VertexData, EdgeData> g = new DirectedSparseHypergraph<>();
//        g.addEdge(e1, Sets.newHashSet(v1), Sets.newHashSet(v2, v3));
        g.addEdge(e2, Sets.newHashSet(v1), Sets.newHashSet(v2));
        g.addEdge(e3, Sets.newHashSet(v4), Sets.newHashSet(v1));
        g.addEdge(e4, Sets.newHashSet(v4), Sets.newHashSet(v2));
        System.out.println(g.toString());
        calculateCentralities(g);
    }

    @Test
    public void undirectedHypergraphCentrality() {
        VertexData v1 = new VertexData(1);
        VertexData v2 = new VertexData(2);
        VertexData v3 = new VertexData(3);
        VertexData v4 = new VertexData(4);

        EdgeData e1 = new EdgeData(1, 4.0, 3.0);
        EdgeData e2 = new EdgeData(2, 2.0, 2.0);
        EdgeData e3 = new EdgeData(3, 1.0, 2.0);

        UndirectedSparseHypergraph<VertexData, EdgeData> g = new UndirectedSparseHypergraph<>();
        g.addEdge(e1, Sets.newHashSet(v1, v2, v3));
        g.addEdge(e2, Sets.newHashSet(v1, v2));
        g.addEdge(e3, Sets.newHashSet(v1, v4));
        System.out.println(g.toString());
        calculateCentralities(g);
    }

    class VertexData {
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

    class EdgeData {
        private Double card;
        private Double freq;
        private int id;

        public EdgeData(int id, Double freq, Double card) {
            this.id = id;
            this.freq = freq;
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
}
