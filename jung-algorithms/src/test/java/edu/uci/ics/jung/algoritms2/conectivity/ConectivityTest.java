package edu.uci.ics.jung.algoritms2.conectivity;

import com.google.common.collect.Sets;
import edu.uci.ics.jung.algorithms2.conectivity.GabowStrongConnectivity;
import edu.uci.ics.jung.algorithms2.conectivity.WeakConectivity;
import edu.uci.ics.jung.algoritms2.util.EdgeData;
import edu.uci.ics.jung.algoritms2.util.VertexData;
import edu.uci.ics.jung.graph.DirectedSparseHypergraph;
import edu.uci.ics.jung.graph.Hypergraph;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class ConectivityTest {

    private void displayResults(List<Set<VertexData>> components) {
        int i = 1;
        for (Set<VertexData> vertices : components) {
            System.out.println("component: " + i);
            System.out.println("vertices: " + Arrays.toString(vertices.stream().sorted(Comparator.comparing(VertexData::getId)).toArray()));
            ++i;
        }
    }

    private void evalComponents(Hypergraph<VertexData, EdgeData> g) {
        List<Set<VertexData>> components;

        System.out.println("*** WCC *** ");
        components = new WeakConectivity<>(g).getConnectedSets();
        displayResults(components);

        System.out.println("*** SCC *** ");
        components = new GabowStrongConnectivity<>(g).getConnectedSets();
        displayResults(components);
    }

    @Test
    public void directedCustomGraphComponents() {
        VertexData v1 = new VertexData(1);
        VertexData v2 = new VertexData(2);
        VertexData v3 = new VertexData(3);
        VertexData v4 = new VertexData(4);
        VertexData v5 = new VertexData(5);

        EdgeData e1 = new EdgeData(1, 1.0, 2.0);
        EdgeData e2 = new EdgeData(2, 1.0, 2.0);
        EdgeData e3 = new EdgeData(3, 1.0, 2.0);
        EdgeData e4 = new EdgeData(4, 1.0, 2.0);

        DirectedSparseHypergraph<VertexData, EdgeData> g = new DirectedSparseHypergraph<>();
        g.addEdge(e1, Sets.newHashSet(v1), Sets.newHashSet(v2));
        g.addEdge(e2, Sets.newHashSet(v3), Sets.newHashSet(v1));
        g.addEdge(e3, Sets.newHashSet(v2), Sets.newHashSet(v3));
        g.addEdge(e4, Sets.newHashSet(v4), Sets.newHashSet(v5));
        System.out.println(g.toString());

        evalComponents(g);
    }

    @Test
    public void directedCustomHypergraphComponents() {
        VertexData v1 = new VertexData(1);
        VertexData v2 = new VertexData(2);
        VertexData v3 = new VertexData(3);
        VertexData v4 = new VertexData(4);
        VertexData v5 = new VertexData(5);

        EdgeData e1 = new EdgeData(1, 1.0, 2.0);
        EdgeData e2 = new EdgeData(2, 1.0, 2.0);

        DirectedSparseHypergraph<VertexData, EdgeData> g = new DirectedSparseHypergraph<>();
        g.addEdge(e1, Sets.newHashSet(v1), Sets.newHashSet(v2, v3));
        g.addEdge(e2, Sets.newHashSet(v4), Sets.newHashSet(v5));
        System.out.println(g.toString());

        evalComponents(g);
    }
}
