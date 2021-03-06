package edu.uci.ics.jung.algoritms2.centrality;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.algorithms2.centrality.BetweennessCentrality;
import edu.uci.ics.jung.algorithms2.centrality.CentralityMode;
import edu.uci.ics.jung.algorithms2.centrality.ClosenessCentrality;
import edu.uci.ics.jung.algorithms2.centrality.DegreeCentrality;
import edu.uci.ics.jung.algoritms2.util.EdgeData;
import edu.uci.ics.jung.algoritms2.util.VertexData;
import edu.uci.ics.jung.graph.DirectedSparseHypergraph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.UndirectedSparseHypergraph;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class CentralityTest {

    private static final boolean SHOULD_NORM = false;
    // normalization -  2 options proposed
    // freqAvg/freq
    // double norm = g.getEdges().stream().mapToDouble(e -> e.getFreq()).average().getAsDouble();
    // freqMax/freq
    // double norm = g.getEdges().stream().mapToDouble(e -> e.getFreq()).max().getAsDouble();
    private static final Function<EdgeData, Double> FREQ_FN = e -> e.getFreq();
    private static final Function<EdgeData, Double> STR_FN = e -> e.getFreq() / (e.getCard() - 1);

    private void evalCentralities(Hypergraph<VertexData, EdgeData> g) {
        DegreeCentrality<VertexData, EdgeData> degreeIn = new DegreeCentrality<>(g, CentralityMode.IN);
        DegreeCentrality<VertexData, EdgeData> degreeOut = new DegreeCentrality<>(g, CentralityMode.OUT);
        DegreeCentrality<VertexData, EdgeData> degree = new DegreeCentrality<>(g, CentralityMode.TOTAL);

        DegreeCentrality<VertexData, EdgeData> degreeInW1 = new DegreeCentrality<>(g, CentralityMode.IN, FREQ_FN);
        DegreeCentrality<VertexData, EdgeData> degreeOutW1 = new DegreeCentrality<>(g, CentralityMode.OUT, FREQ_FN);
        DegreeCentrality<VertexData, EdgeData> degreeW1 = new DegreeCentrality<>(g, CentralityMode.TOTAL, FREQ_FN);

        DegreeCentrality<VertexData, EdgeData> degreeInW2 = new DegreeCentrality<>(g, CentralityMode.IN, STR_FN);
        DegreeCentrality<VertexData, EdgeData> degreeOutW2 = new DegreeCentrality<>(g, CentralityMode.OUT, STR_FN);
        DegreeCentrality<VertexData, EdgeData> degreeW2 = new DegreeCentrality<>(g, CentralityMode.TOTAL, STR_FN);

        ClosenessCentrality<VertexData, EdgeData> closeness = new ClosenessCentrality<>(g, SHOULD_NORM, false);
        ClosenessCentrality<VertexData, EdgeData> closenessW1 = new ClosenessCentrality<>(g, FREQ_FN, SHOULD_NORM, false);
        ClosenessCentrality<VertexData, EdgeData> closenessW2 = new ClosenessCentrality<>(g, STR_FN, SHOULD_NORM, false);

        BetweennessCentrality<VertexData, EdgeData> betweenness = new BetweennessCentrality<>(g, SHOULD_NORM);
        BetweennessCentrality<VertexData, EdgeData> betweennessW1 = new BetweennessCentrality<>(g, FREQ_FN, SHOULD_NORM);
        BetweennessCentrality<VertexData, EdgeData> betweennessW2 = new BetweennessCentrality<>(g, STR_FN, SHOULD_NORM);

        List<VertexData> vertices = g.getVertices()
                                    .stream()
                                    .sorted(Comparator.comparing(VertexData::getId))
                                    .collect(Collectors.toList());

        System.out.println("degree (unweighted)");
        vertices.forEach(v -> System.out.println(v + " | " + degreeIn.getVertexScore(v) + " | " + degreeOut.getVertexScore(v) + " | " + degree.getVertexScore(v)));

        System.out.println("degree (freq)");
        vertices.forEach(v -> System.out.println(v + " | " + degreeInW1.getVertexScore(v) + " | " + degreeOutW1.getVertexScore(v) + " | " + degreeW1.getVertexScore(v)));

        System.out.println("degree (strength)");
        vertices.forEach(v -> System.out.println(v + " | " + degreeInW2.getVertexScore(v) + " | " + degreeOutW2.getVertexScore(v) + " | " + degreeW2.getVertexScore(v)));

        System.out.println("closeness (unweighted)");
        vertices.forEach(v -> System.out.println(v + " | " + closeness.getVertexScore(v)));

        System.out.println("closeness (freq)");
        vertices.forEach(v -> System.out.println(v + " | " + closenessW1.getVertexScore(v)));

        System.out.println("closeness (strength)");
        vertices.forEach(v -> System.out.println(v + " | " + closenessW2.getVertexScore(v)));

        System.out.println("betweenness (unweighted)");
        vertices.forEach(v -> System.out.println(v + " | " + betweenness.getVertexScore(v)));

        System.out.println("betweenness (freq)");
        vertices.forEach(v -> System.out.println(v + " | " + betweennessW1.getVertexScore(v)));

        System.out.println("betweenness (strength)");
        vertices.forEach(v -> System.out.println(v + " | " + betweennessW2.getVertexScore(v)));
    }

    @Test
    public void directedCustomGraphCentrality() {
        VertexData v1 = new VertexData(1);
        VertexData v2 = new VertexData(2);
        VertexData v3 = new VertexData(3);

        EdgeData e1 = new EdgeData(1, 1.0, 2.0);
        EdgeData e2 = new EdgeData(2, 1.0, 2.0);
        EdgeData e3 = new EdgeData(3, 1.0, 2.0);

        DirectedSparseHypergraph<VertexData, EdgeData> g = new DirectedSparseHypergraph<>();
        g.addEdge(e1, Sets.newHashSet(v1), Sets.newHashSet(v2));
        g.addEdge(e2, Sets.newHashSet(v3), Sets.newHashSet(v1));
        g.addEdge(e3, Sets.newHashSet(v2), Sets.newHashSet(v3));
        System.out.println(g.toString());

        evalCentralities(g);
    }

    @Test
    public void undirectedCustomHypergraphCentrality() {
        // degree (unweighted)
        // V1 | 1.0 | 1.0 | 1.0
        // V2 | 1.0 | 1.0 | 1.0
        // V3 | 1.0 | 1.0 | 1.0
        // V4 | 2.0 | 2.0 | 2.0
        // V5 | 1.0 | 1.0 | 1.0
        // V6 | 1.0 | 1.0 | 1.0
        // closeness (unweighted)
        // V1 | 0.14285714285714285
        // V2 | 0.14285714285714285
        // V3 | 0.14285714285714285
        // V4 | 0.2
        // V5 | 0.125
        // V6 | 0.125
        // betweenness (unweighted)
        // V1 | 0.0
        // V2 | 0.0
        // V3 | 0.0
        // V4 | 6.0
        // V5 | 0.0
        // V6 | 0.0
        VertexData v1 = new VertexData(1);
        VertexData v2 = new VertexData(2);
        VertexData v3 = new VertexData(3);
        VertexData v4 = new VertexData(4);
        VertexData v5 = new VertexData(5);
        VertexData v6 = new VertexData(6);

        EdgeData e1 = new EdgeData(1, 1.0, 4.0);
        EdgeData e2 = new EdgeData(2, 1.0, 3.0);

        UndirectedSparseHypergraph<VertexData, EdgeData> g = new UndirectedSparseHypergraph<>();
        g.addEdge(e1, Sets.newHashSet(v1, v2, v3, v4));
        g.addEdge(e2, Sets.newHashSet(v4, v5, v6));
        System.out.println(g.toString());

        evalCentralities(g);
    }

    @Test
    public void undirectedCustomGraphCentrality() {
        //    v5----v4----v6
        //   /|     |
        //  / |     |
        // v1 |     |
        //  \ |     |
        //   \|     |
        //    v2----v3
        // degree (unweighted)
        // V1 | 2.0 | 2.0 | 2.0
        // V2 | 3.0 | 3.0 | 3.0
        // V3 | 2.0 | 2.0 | 2.0
        // V4 | 3.0 | 3.0 | 3.0
        // V5 | 3.0 | 3.0 | 3.0
        // V6 | 1.0 | 1.0 | 1.0
        // closeness (unweighted)
        // V1 | 0.1111111111111111
        // V2 | 0.125
        // V3 | 0.125
        // V4 | 0.14285714285714285
        // V5 | 0.14285714285714285
        // V6 | 0.09090909090909091
        // betweenness (unweighted)
        // V1 | 0.0
        // V2 | 1.5
        // V3 | 1.0
        // V4 | 4.5
        // V5 | 3.0
        // V6 | 0.0
        VertexData v1 = new VertexData(1);
        VertexData v2 = new VertexData(2);
        VertexData v3 = new VertexData(3);
        VertexData v4 = new VertexData(4);
        VertexData v5 = new VertexData(5);
        VertexData v6 = new VertexData(6);

        EdgeData e1 = new EdgeData(1, 1.0, 2.0);
        EdgeData e2 = new EdgeData(2, 5.0, 2.0);
        EdgeData e3 = new EdgeData(3, 3.0, 2.0);
        EdgeData e4 = new EdgeData(4, 2.0, 2.0);
        EdgeData e5 = new EdgeData(5, 6.0, 2.0);
        EdgeData e6 = new EdgeData(6, 4.0, 2.0);
        EdgeData e7 = new EdgeData(7, 1.0, 2.0);

        UndirectedSparseHypergraph<VertexData, EdgeData> g = new UndirectedSparseHypergraph<>();
        g.addEdge(e1, Sets.newHashSet(v1, v2));
        g.addEdge(e2, Sets.newHashSet(v2, v3));
        g.addEdge(e3, Sets.newHashSet(v3, v4));
        g.addEdge(e4, Sets.newHashSet(v4, v5));
        g.addEdge(e5, Sets.newHashSet(v2, v5));
        g.addEdge(e6, Sets.newHashSet(v1, v5));
        g.addEdge(e7, Sets.newHashSet(v4, v6));
        System.out.println(g.toString());

        evalCentralities(g);
    }

    @Test
    public void undirectedStarGraphCentrality() {
        // star
        // degree (unweighted)
        // V1 | 5.0 | 5.0 | 5.0
        // V2 | 1.0 | 1.0 | 1.0
        // V3 | 1.0 | 1.0 | 1.0
        // V4 | 1.0 | 1.0 | 1.0
        // V5 | 1.0 | 1.0 | 1.0
        // V6 | 1.0 | 1.0 | 1.0
        // closeness (unweighted, non-norm)
        // V1 | 0.2
        // V2 | 0.1111111111111111
        // V3 | 0.1111111111111111
        // V4 | 0.1111111111111111
        // V5 | 0.1111111111111111
        // V6 | 0.1111111111111111
        // betweenness (unweighted, non-norm)
        // V1 | 10.0
        // V2 | 0.0
        // V3 | 0.0
        // V4 | 0.0
        // V5 | 0.0
        // V6 | 0.0

        VertexData v1 = new VertexData(1);
        VertexData v2 = new VertexData(2);
        VertexData v3 = new VertexData(3);
        VertexData v4 = new VertexData(4);
        VertexData v5 = new VertexData(5);
        VertexData v6 = new VertexData(6);

        EdgeData e1 = new EdgeData(1, 1.0, 2.0);
        EdgeData e2 = new EdgeData(2, 1.0, 2.0);
        EdgeData e3 = new EdgeData(3, 1.0, 2.0);
        EdgeData e4 = new EdgeData(4, 1.0, 2.0);
        EdgeData e5 = new EdgeData(5, 1.0, 2.0);

        UndirectedSparseHypergraph<VertexData, EdgeData> g = new UndirectedSparseHypergraph<>();
        g.addEdge(e1, Sets.newHashSet(v1, v2));
        g.addEdge(e2, Sets.newHashSet(v1, v3));
        g.addEdge(e3, Sets.newHashSet(v1, v4));
        g.addEdge(e4, Sets.newHashSet(v1, v5));
        g.addEdge(e5, Sets.newHashSet(v1, v6));
        System.out.println(g.toString());

        evalCentralities(g);
    }

    @Test
    public void undirectedCustomGraphCentrality2() {
        //         v3
        //        /  \
        //       /    \
        // v1---v2     v5
        //       \    /
        //        \  /
        //         v4
        // degree (unweighted)
        // V1 | 1.0 | 1.0 | 1.0
        // V2 | 3.0 | 3.0 | 3.0
        // V3 | 2.0 | 2.0 | 2.0
        // V4 | 2.0 | 2.0 | 2.0
        // V5 | 2.0 | 2.0 | 2.0
        // closeness (unweighted, non-norm)
        // V1 | 0.125
        // V2 | 0.2
        // V3 | 0.16666666666666666
        // V4 | 0.16666666666666666
        // V5 | 0.14285714285714285
        // betweenness (unweighted, non-norm)
        // V1 | 0.0
        // V2 | 3.5
        // V3 | 1.0
        // V4 | 1.0
        // V5 | 0.5
        VertexData v1 = new VertexData(1);
        VertexData v2 = new VertexData(2);
        VertexData v3 = new VertexData(3);
        VertexData v4 = new VertexData(4);
        VertexData v5 = new VertexData(5);

        EdgeData e1 = new EdgeData(1, 1.0, 2.0);
        EdgeData e2 = new EdgeData(2, 1.0, 2.0);
        EdgeData e3 = new EdgeData(3, 2.0, 2.0);
        EdgeData e4 = new EdgeData(4, 1.0, 2.0);
        EdgeData e5 = new EdgeData(5, 2.0, 2.0);

        UndirectedSparseHypergraph<VertexData, EdgeData> g = new UndirectedSparseHypergraph<>();
        g.addEdge(e1, Sets.newHashSet(v1, v2));
        g.addEdge(e2, Sets.newHashSet(v2, v3));
        g.addEdge(e3, Sets.newHashSet(v2, v4));
        g.addEdge(e4, Sets.newHashSet(v3, v5));
        g.addEdge(e5, Sets.newHashSet(v4, v5));
        System.out.println(g.toString());

        evalCentralities(g);
    }

    @Test
    public void undirectedLineGraphCentrality() {
        // v1---v2---v3---v4---v5
        // degree (unweighted)
        // V1 | 1.0 | 1.0 | 1.0
        // V2 | 2.0 | 2.0 | 2.0
        // V3 | 2.0 | 2.0 | 2.0
        // V4 | 2.0 | 2.0 | 2.0
        // V5 | 1.0 | 1.0 | 1.0
        // closeness (unweighted, non-norm)
        // V1 | 0.1
        // V2 | 0.14285714285714285
        // V3 | 0.16666666666666666
        // V4 | 0.14285714285714285
        // V5 | 0.1
        // betweenness (unweighted, non-norm)
        // V1 | 0.0
        // V2 | 3.0
        // V3 | 4.0
        // V4 | 3.0
        // V5 | 0.0
        VertexData v1 = new VertexData(1);
        VertexData v2 = new VertexData(2);
        VertexData v3 = new VertexData(3);
        VertexData v4 = new VertexData(4);
        VertexData v5 = new VertexData(5);

        EdgeData e1 = new EdgeData(1, 1.0, 2.0);
        EdgeData e2 = new EdgeData(2, 1.0, 2.0);
        EdgeData e3 = new EdgeData(3, 2.0, 2.0);
        EdgeData e4 = new EdgeData(4, 1.0, 2.0);

        UndirectedSparseHypergraph<VertexData, EdgeData> g = new UndirectedSparseHypergraph<>();
        g.addEdge(e1, Sets.newHashSet(v1, v2));
        g.addEdge(e2, Sets.newHashSet(v2, v3));
        g.addEdge(e3, Sets.newHashSet(v3, v4));
        g.addEdge(e4, Sets.newHashSet(v4, v5));
        System.out.println(g.toString());

        evalCentralities(g);
    }
}
