package edu.uci.ics.jung.algorithms2.conectivity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.AbstractHypergraph;
import edu.uci.ics.jung.graph.Hypergraph;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Computes the strongly connected components of a directed graph. The implemented algorithm follows
 * Cheriyan-Mehlhorn/Gabow's algorithm presented in Path-based depth-first search for strong and
 * biconnected components by Gabow (2000). The running time is order of $O(|V|+|E|)$.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Sarah Komla-Ebri
 */
public class GabowStrongConnectivity<V, E> extends AbstractStrongConnectivity<V, E> {

    // stores the vertices
    private Deque<VertexNumber<V>> stack = new ArrayDeque<>();

    // maps vertices to their VertexNumber object
    private Map<V, VertexNumber<V>> vertexToVertexNumber;

    // store the numbers
    private Deque<Integer> B = new ArrayDeque<>();

    // number of vertices
    private int c;

    /**
     * Constructor
     *
     * @param graph the graph to inspect
     * @throws NullPointerException in case the graph is null
     */
    public GabowStrongConnectivity(Hypergraph<V, E> graph) {
        super(graph);
    }

    @Override
    public List<Set<V>> getConnectedSets() {
        if (connectedSets == null) {
            connectedSets = new LinkedList<>();

            // create VertexData objects for all vertices, store them
            createVertexNumber();

            // perform DFS
            for (VertexNumber<V> data : vertexToVertexNumber.values()) {
                if (data.getNumber() == 0) {
                    dfsVisit(graph, data);
                }
            }

            vertexToVertexNumber = null;
            stack = null;
            B = null;
        }

        return connectedSets;
    }

    /*
     * Creates a VertexNumber object for every vertex in the graph and stores them in a HashMap.
     */

    private void createVertexNumber() {
        c = graph.getVertices().size();
        vertexToVertexNumber = new HashMap<>(c);

        for (V vertex : graph.getVertices()) {
            vertexToVertexNumber.put(vertex, new VertexNumber<>(vertex, 0));
        }

        stack = new ArrayDeque<>(c);
        B = new ArrayDeque<>(c);
    }

    /*
     * The subroutine of DFS.
     */
    private void dfsVisit(Hypergraph<V, E> visitedGraph, VertexNumber<V> vn) {
        VertexNumber<V> wn;
        stack.add(vn);
        B.add(vn.setNumber(stack.size() - 1));

        // follow all edges

        Multimap<V, E> nbs = getNeighbors(vn.getVertex(), visitedGraph.getOutEdges(vn.getVertex()));
        for (Map.Entry<V, E> nb : nbs.entries()) {
            wn = vertexToVertexNumber.get(nb.getKey());

            if (wn.getNumber() == 0) {
                dfsVisit(graph, wn);
            } else { /* contract if necessary */
                while (wn.getNumber() < B.getLast()) {
                    B.removeLast();
                }
            }
        }

        Set<V> L = new HashSet<>();
        if (vn.getNumber() == (B.getLast())) {
            /*
             * number vertices of the next strong component
             */
            B.removeLast();

            c++;
            while (vn.getNumber() <= (stack.size() - 1)) {
                VertexNumber<V> r = stack.removeLast();
                L.add(r.getVertex());
                r.setNumber(c);
            }
            connectedSets.add(L);
        }
    }

    private Multimap<V, E> getNeighbors(V v, Collection<E> vEdges) {
        //resolve hyper-neighbors
        Multimap<V, E> v_neighbors = HashMultimap.create();
        for (E e : vEdges) {
            if (graph instanceof AbstractHypergraph) {
                for (V n : ((AbstractHypergraph<V, E>) graph).getOpposite(v, e)) {
                    if (!n.equals(v) && !v_neighbors.containsEntry(n, e)) {
                        v_neighbors.put(n, e);
                    }
                }
            } else {
                V n = ((AbstractGraph<V, E>) graph).getOpposite(v, e);
                if (!n.equals(v) && !v_neighbors.containsEntry(n, e)) {
                    v_neighbors.put(n, e);
                }
            }
        }
        return v_neighbors;
    }

    private static final class VertexNumber<V> {

        V vertex;
        int number;

        private VertexNumber(V vertex, int number) {
            this.vertex = vertex;
            this.number = number;
        }

        int getNumber() {
            return number;
        }

        V getVertex() {
            return vertex;
        }

        Integer setNumber(int n) {
            return number = n;
        }
    }
}