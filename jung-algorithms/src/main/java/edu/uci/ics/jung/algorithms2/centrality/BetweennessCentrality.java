package edu.uci.ics.jung.algorithms2.centrality;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.MoreObjects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import edu.uci.ics.jung.algorithms.scoring.EdgeScorer;
import edu.uci.ics.jung.algorithms.scoring.VertexScorer;
import edu.uci.ics.jung.algorithms2.util.FibonacciHeap;
import edu.uci.ics.jung.algorithms2.util.FibonacciHeapNode;
import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.AbstractHypergraph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedHypergraph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Computes betweenness centrality for each vertex and edge in the graph.
 * <br>
 * Let σ(s, t) denote the total number of shortest paths from s to t in G and,
 * for any v ∈ V , let σ(s, t | v) denote the number of shortest paths from s to t in G that go through v.
 * Note that σ(s, s) = 1, and σ(s, t | v) = 0 if v ∈ {s, t} or if v does not lie on any shortest path from s to t.
 * <br>
 * Similarly, for any edge e ∈ E, let σ(s, t | e) denote the number of shortest paths from s to t in G that go through e.
 * The betweenness centrality of a vertex v is the sum over all pairs of vertices
 * of the fractional count of shortest paths going through v.
 *
 * @see "Ulrik Brandes: A Faster Algorithm for Betweenness Centrality. Journal of Mathematical Sociology 25(2):163-177, 2001."
 * @see <a href=https://github.com/jgrapht/jgrapht/blob/master/jgrapht-core/src/main/java/org/jgrapht/alg/scoring/BetweennessCentrality.java>jgrapht implementation</a>
 * @see <a href=https://github.com/graphstream/gs-algo/blob/master/src/org/graphstream/algorithm/BetweennessCentrality.java>graphstream implementation</a>
 */
public class BetweennessCentrality<V, E> implements VertexScorer<V, Double>, EdgeScorer<E, Double> {

    private Hypergraph<V, E> graph;

    private Map<V, Double> vertex_scores;

    private Map<E, Double> edge_scores;

    private Map<V, BetweennessVertexData> vertex_data;

    /**
     * Calculates betweenness scores based on the all-pairs unweighted shortest paths
     * in the graph.
     *
     * @param graph the graph for which the scores are to be calculated
     */
    public BetweennessCentrality(Hypergraph<V, E> graph, boolean normalize) {
        initialize(graph);
        calculateBetweenness(
            false,
            Functions.<Double>constant(1.0),
            normalize);
    }

    /**
     * Calculates betweenness scores based on the all-pairs weighted shortest paths in the
     * graph.
     * <p>
     * <p>NOTE: This version of the algorithm may not work correctly on all graphs; we're still
     * working out the bugs.  Use at your own risk.
     *
     * @param graph the graph for which the scores are to be calculated
     * @param edge_weights the edge weights to be used in the path length calculations
     */
    public BetweennessCentrality(Hypergraph<V, E> graph, Function<? super E, Double> edge_weights, boolean normalize) {
        // reject negative-weight edges up front
        for (E e : graph.getEdges()) {
            double e_weight = edge_weights.apply(e);
            if (e_weight < 0) {
                throw new IllegalArgumentException(String.format("Weight for edge '%s' is < 0: %d", e, e_weight));
            }
        }

        initialize(graph);
        calculateBetweenness(
            true,
            edge_weights,
            normalize);
    }

    private void initialize(Hypergraph<V, E> graph) {
        this.graph = graph;
        this.vertex_scores = new HashMap<V, Double>();
        this.edge_scores = new HashMap<E, Double>();
        this.vertex_data = new HashMap<V, BetweennessVertexData>();

        for (V v : graph.getVertices()) {
            this.vertex_scores.put(v, 0.0);
        }

        for (E e : graph.getEdges()) {
            this.edge_scores.put(e, 0.0);
        }
    }

    private void calculateBetweenness(boolean weighted, Function<? super E, Double> edge_weights, boolean normalize) {
        for (V s : graph.getVertices()) {
            // System.out.println();
            // System.out.println("--- s ---");
            // System.out.println("s: " + s.toString());

            // initialize the betweenness data for this new vertex
            for (V v : graph.getVertices()) {
                vertex_data.put(v, new BetweennessVertexData());
            }

            vertex_data.get(s).pathCount = 1; //sigma
            vertex_data.get(s).distance = 0; //d

            ArrayDeque<V> S = new ArrayDeque<V>(graph.getVertexCount());

            CustomQueue<V,Double> Q;
            if(weighted) {
                Q = new WeightedQueue();
            } else {
                Q = new UnweightedQueue();
            }
            Q.insert(s, 0.0);

            // System.out.println();
            // System.out.println("--- path count ---");

            // 1. compute the length and the number of shortest paths between all s to v
            while (!Q.isEmpty()) {
                V v = Q.remove();
                S.push(v);

                BetweennessVertexData v_data = vertex_data.get(v);

                Multimap<V, E> v_opposite = getNeighbors(v, graph.getOutEdges(v));
                // System.out.println();
                // System.out.println("v: " + v.toString() + "; data: " + v_data.toString());
                // System.out.println("neighbors: " + v_opposite.toString());

                for (Map.Entry<V, E> v_opposite_entry : v_opposite.entries()) {
                    // System.out.println();
                    // System.out.println("edge(v-w): " + v_opposite_entry.getValue().toString());

                    V w = v_opposite_entry.getKey();
                    Double wv_weight = edge_weights.apply(v_opposite_entry.getValue());
                    BetweennessVertexData w_data = vertex_data.get(w);
                    Double dist = v_data.distance + wv_weight;

                    // System.out.println(" w: " + w.toString() + "; data: " + w_data.toString());

                    // w found for the first time?
                    if (w_data.distance == Double.POSITIVE_INFINITY) {
                        Q.insert(w, dist);
                        w_data.distance = dist;
                    }

                    // shortest path to w via v?
                    if (w_data.distance >= dist) {
                        w_data.distance = dist;
                        Q.update(w, dist);
                        // MapBinaryHeap<V> tmp = (MapBinaryHeap<V>) Q;
                        // if(tmp.contains(w)) {
                        //     tmp.update(w);
                        // }

                        w_data.pathCount = w_data.pathCount + v_data.pathCount;
                        w_data.pred.add(new PredData(v_opposite_entry.getValue(), v));
                        // System.out.println("*w: " + w.toString() + "; data: " + w_data.toString());
                    }
                }
            }

            // System.out.println();
            // System.out.println("--- pair-dependency ---");

            // 2. sum all pair dependencies. The pair-dependency of s and v in w
            // S returns vertices in order of non-increasing distance from s
            while (!S.isEmpty()) {
                V w = S.pop();

                // System.out.println();
                // System.out.println(" w: " + w.toString() + "; data: " + vertex_data.get(w).toString());

                for (PredData pred : vertex_data.get(w).pred) {
                    // System.out.println(" v: " + v.toString() + "; data: " + vertex_data.get(v).toString());

                    Double delta = (vertex_data.get(pred.v).pathCount / vertex_data.get(w).pathCount) * (1.0 + vertex_data.get(w).delta);
                    if(delta > 0) {
                        vertex_data.get(pred.v).delta = vertex_data.get(pred.v).delta + delta;
                        // System.out.println("*v: " + v.toString() + "; data: " + vertex_data.get(v).toString());
                    }

                    Double e_score = edge_scores.get(pred.e) + delta;
                    edge_scores.put(pred.e, e_score);
                }

                if (!w.equals(s)) {
                    Double w_score = vertex_scores.get(w) + vertex_data.get(w).delta;
                    // System.out.println("*w: " + w.toString() + "; bc: " + vertex_scores.get(w) + " -> " + w_score);
                    vertex_scores.put(w, w_score);
                }
            }
        }

        // For undirected graph, divide scores by two as each shortest path considered twice.
        if ((graph instanceof UndirectedGraph || graph instanceof UndirectedHypergraph)) {
            for (V v : graph.getVertices()) {
                double v_score = vertex_scores.get(v);
                v_score /= 2.0;
                vertex_scores.put(v, v_score);
            }
            for (E e : graph.getEdges()) {
                double e_score = edge_scores.get(e);
                e_score /= 2.0;
                edge_scores.put(e, e_score);
            }
        }

        vertex_data.clear();
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

    @Override
    public Double getEdgeScore(E e) {
        return edge_scores.get(e);
    }

    @Override
    public Double getVertexScore(V v) {
        return vertex_scores.get(v);
    }

    private class BetweennessVertexData {
        double distance; //d
        double pathCount; //sigma
        List<PredData> pred;
        double delta;

        BetweennessVertexData() {
            distance = Double.POSITIVE_INFINITY;
            pathCount = 0;
            pred = new ArrayList<>();
            delta = 0;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                              .add("distance", distance)
                              .add("pathCount", pathCount)
                              .add("pred", Arrays.deepToString(pred.toArray()))
                              .add("delta", delta)
                              .toString();
        }
    }

    private class PredData {
        E e;
        V v;

        PredData(E edge, V vertex) {
            this.e = edge;
            this.v = vertex;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                              .add("e", e)
                              .add("v", v)
                              .toString();
        }
    }

    private interface CustomQueue<T, D> {

        void insert(T t, D d);

        void update(T t, D d);

        T remove();

        boolean isEmpty();
    }

    private class WeightedQueue implements CustomQueue<V, Double> {

        FibonacciHeap<V> delegate = new FibonacciHeap<>();
        Map<V, FibonacciHeapNode<V>> seen = new HashMap<>();

        @Override
        public void insert(V t, Double d) {
            FibonacciHeapNode<V> node = new FibonacciHeapNode<>(t);
            delegate.insert(node, d);
            seen.put(t, node);
        }

        @Override
        public void update(V t, Double d) {
            if (!seen.containsKey(t)) {
                throw new IllegalArgumentException("Element " + t + " does not exist in queue");
            }
            delegate.decreaseKey(seen.get(t), d);
        }

        @Override
        public V remove() {
            return delegate.removeMin().getData();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

    }

    private class UnweightedQueue implements CustomQueue<V, Double> {

        Queue<V> delegate = new ArrayDeque<>();

        @Override
        public void insert(V t, Double d) {
            delegate.add(t);
        }

        @Override
        public void update(V t, Double d) {
            // do nothing
        }

        @Override
        public V remove() {
            return delegate.remove();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

    }
}
