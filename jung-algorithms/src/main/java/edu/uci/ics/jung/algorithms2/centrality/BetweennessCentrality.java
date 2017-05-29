package edu.uci.ics.jung.algorithms2.centrality;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import edu.uci.ics.jung.algorithms.scoring.EdgeScorer;
import edu.uci.ics.jung.algorithms.scoring.VertexScorer;
import edu.uci.ics.jung.algorithms.util.MapBinaryHeap;
import edu.uci.ics.jung.graph.*;

import java.util.*;

/**
 * Computes betweenness centrality for each vertex and edge in the graph.
 *
 * @see "Ulrik Brandes: A Faster Algorithm for Betweenness Centrality. Journal of Mathematical Sociology 25(2):163-177, 2001."
 */
public class BetweennessCentrality<V, E> implements VertexScorer<V, Double>, EdgeScorer<E, Double> {

    private Hypergraph<V, E> graph;

    private Map<V, Double> vertex_scores;

    private Map<E, Double> edge_scores;

    private Map<V, BetweennessData> vertex_data;

    /**
     * Calculates betweenness scores based on the all-pairs unweighted shortest paths
     * in the graph.
     *
     * @param graph the graph for which the scores are to be calculated
     */
    public BetweennessCentrality(Hypergraph<V, E> graph, boolean normalize) {
        initialize(graph);
        calculateBetweenness(new LinkedList<V>(), Functions.<Double>constant(1.0), normalize);
//        calculateBetweenness(graph, Functions.<Double>constant(1.0), normalize);
    }

    /**
     * Calculates betweenness scores based on the all-pairs weighted shortest paths in the
     * graph.
     * <p>
     * <p>NOTE: This version of the algorithm may not work correctly on all graphs; we're still
     * working out the bugs.  Use at your own risk.
     *
     * @param graph        the graph for which the scores are to be calculated
     * @param edge_weights the edge weights to be used in the path length calculations
     */
    public BetweennessCentrality(Hypergraph<V, E> graph,
                                 Function<? super E, Double> edge_weights, boolean normalize) {
        // reject negative-weight edges up front
        for (E e : graph.getEdges()) {
            double e_weight = edge_weights.apply(e).doubleValue();
            if (e_weight < 0)
                throw new IllegalArgumentException(String.format("Weight for edge '%s' is < 0: %d", e, e_weight));
        }

        initialize(graph);
        calculateBetweenness(new MapBinaryHeap<V>(new BetweennessComparator()), edge_weights, normalize);
//        calculateBetweenness(graph, edge_weights, normalize);
    }

    private void calculateBetweenness(Hypergraph<V, E> graph, Function<? super E, Double> edge_weights, boolean normalize) {
        Map<V, BetweennessData> vertexData = new HashMap<V, BetweennessData>();
        Map<V, Number> bcVertexScore = Maps.newHashMap();

        //init
        for (V v : graph.getVertices()) {
            bcVertexScore.put(v, 0.0);
        }

        for (V s : graph.getVertices()) {//init
            for (V v : graph.getVertices()) {
                vertexData.put(v, new BetweennessData());
            }
            vertexData.get(s).pathCount = 1;
            vertexData.get(s).distance = 0;

            Stack<V> stack = new Stack<V>();
            Queue<V> queue = new LinkedList<V>();
            queue.add(s);

            while (!queue.isEmpty()) {
                V v = queue.poll();
                stack.push(v);

                for (V w : graph.getSuccessors(v)) {
                    if (vertexData.get(w).distance < 0) {
                        queue.offer(w);
                        vertexData.get(w).distance = vertexData.get(v).distance + 1;
                    }
                    if (vertexData.get(w).distance == vertexData.get(v).distance + 1) {
                        vertexData.get(w).pathCount += vertexData.get(v).pathCount;
                        vertexData.get(w).predecessors.add(v);
                    }
                }
            }

            while (!stack.isEmpty()) {
                V w = stack.pop();
                for (V v : vertexData.get(w).predecessors) {
                    double delta = (vertexData.get(v).pathCount / vertexData.get(w).pathCount);
                    delta *= (1.0 + vertexData.get(w).delta);
                    vertexData.get(v).delta += delta;
                }
                if (!w.equals(s)) {
                    double bcValue = bcVertexScore.get(w).doubleValue();
                    bcValue += vertexData.get(w).delta;
                    bcVertexScore.put(w, bcValue);
                }
            }
        }

        for (V vertex : graph.getVertices()) {
            vertexData.remove(vertex);
        }
    }

    private void calculateBetweenness(Function<? super E, Double> edge_weights, boolean normalize) {
        for (V s : graph.getVertices()) {
            // initialize the betweenness data for this new vertex
            for (V t : graph.getVertices())
                vertex_data.put(t, new BetweennessData());
            vertex_data.get(s).pathCount = 1; //sigma
            vertex_data.get(s).distance = 0;

            PriorityQueue<V> stack = new PriorityQueue<V>(graph.getVertexCount(), new BrandesNodeComparatorLargerFirst());
            PriorityQueue<V> queue = new PriorityQueue<V>(graph.getVertexCount(), new BrandesNodeComparatorSmallerFirst());
            queue.offer(s);

            while (!queue.isEmpty()) {
                V v = queue.poll();
                stack.offer(v);
                BetweennessData v_data = vertex_data.get(v);

                Multimap<V, E> v_neighbors = getNeighbors(v, graph.getOutEdges(v));
                for (Map.Entry<V, E> v_neighbors_entry : v_neighbors.entries()) {
                    V w = v_neighbors_entry.getKey();
                    Double vw_weight = edge_weights.apply(v_neighbors_entry.getValue()).doubleValue();
                    BetweennessData w_data = vertex_data.get(w);
                    Double w_alt_dist = v_data.distance + vw_weight;

                    if (w_alt_dist < w_data.distance) {
                        w_data.distance = w_alt_dist;
                        if (queue.contains(w)) {
                            queue.remove(w);
                            queue.offer(w);
                        }
                        if (stack.contains(w)) {
                            stack.remove(w);
                            stack.offer(w);
                        }
                        if (w_data.distance < 0) {
                            queue.offer(w);
                        }
                        w_data.incomingEdges.clear();
                        w_data.pathCount = 0.0;
                    }

                    if (w_data.distance == w_alt_dist) {
                        w_data.pathCount += v_data.pathCount;
                        w_data.incomingEdges.add(v_neighbors_entry.getValue());
                    }
                }
            }

            // S returns vertices in order of non-increasing distance from s
            while (!stack.isEmpty()) {
                V w = stack.poll();
                Multimap<V, E> w_neighbors = getNeighbors(w, vertex_data.get(w).incomingEdges);
                for (Map.Entry<V, E> w_neighbors_entry : w_neighbors.entries()) {
                    V v = w_neighbors_entry.getKey();

                    Double c = vertex_data.get(v).pathCount / vertex_data.get(w).pathCount * (1.0 + vertex_data.get(w).delta);
                    vertex_data.get(v).delta += c;

                    Double e_score = edge_scores.get(w_neighbors_entry.getValue()).doubleValue() + c;
                    edge_scores.put(w_neighbors_entry.getValue(), e_score);
                }
                if (!w.equals(s)) {
                    Double w_score = vertex_scores.get(w).doubleValue() + vertex_data.get(w).delta;
                    vertex_scores.put(w, w_score);
                }
            }
        }

        if (normalize && graph instanceof UndirectedGraph || graph instanceof UndirectedHypergraph) {
            for (V v : graph.getVertices()) {
                double v_score = vertex_scores.get(v).doubleValue();
                v_score /= 2.0;
                vertex_scores.put(v, v_score);
            }
            for (E e : graph.getEdges()) {
                double e_score = edge_scores.get(e).doubleValue();
                e_score /= 2.0;
                edge_scores.put(e, e_score);
            }
        }

        vertex_data.clear();
    }


    private void calculateBetweenness(Queue<V> queue,
                                    Function<? super E, Double> edge_weights,
                                    boolean normalize) {
        for (V s : graph.getVertices()) {
            // initialize the betweenness data for this new vertex
            for (V t : graph.getVertices())
                vertex_data.put(t, new BetweennessData());

            vertex_data.get(s).pathCount = 1; //sigma
            vertex_data.get(s).distance = 0; //d

            Stack<V> stack = new Stack<V>();

            queue.offer(s);

            while (!queue.isEmpty()) {
                V v = queue.poll();
                stack.push(v);
                BetweennessData v_data = vertex_data.get(v);

                Multimap<V, E> v_neighbors = getNeighbors(v, graph.getOutEdges(v));
                for (Map.Entry<V, E> v_neighbors_entry : v_neighbors.entries()) {
                    V w = v_neighbors_entry.getKey();
                    Double wv_weight = edge_weights.apply(v_neighbors_entry.getValue()).doubleValue();
                    BetweennessData w_data = vertex_data.get(w);
                    Double w_alt_dist = v_data.distance + wv_weight;

                    if (w_data.distance < 0) {
                        queue.offer(w);
                        w_data.distance = w_alt_dist;
                    }

                    // note: this can only happen with weighted edges
                    if (w_data.distance > w_alt_dist) {
                        w_data.distance = w_alt_dist;
                        w_data.pathCount = 0.0;
                        // invalidate previously identified incoming edges (we have a new shortest path distance to x)
                        w_data.incomingEdges.clear();
                        // update w's position in queue
                        MapBinaryHeap<V> tmp = (MapBinaryHeap<V>) queue;
                        if(tmp.contains(w)) {
                            tmp.update(w);
                        }
                        if (stack.contains(w)) {
                            stack.remove(w);
                            stack.push(w);
                        }
                    }

                    if (w_data.distance == w_alt_dist) {
                        w_data.pathCount = w_data.pathCount + v_data.pathCount;
                        w_data.incomingEdges.add(v_neighbors_entry.getValue());
                    }
                }
            }

            // S returns vertices in order of non-increasing distance from s
            while (!stack.isEmpty()) {
                V w = stack.pop();
                Multimap<V, E> w_neighbors = getNeighbors(w, vertex_data.get(w).incomingEdges);
                for (Map.Entry<V, E> w_neighbors_entry : w_neighbors.entries()) {
                    V v = w_neighbors_entry.getKey();

                    Double c = (vertex_data.get(v).pathCount / vertex_data.get(w).pathCount) * (1.0 + vertex_data.get(w).delta);
                    vertex_data.get(v).delta += c;

                    Double e_score = edge_scores.get(w_neighbors_entry.getValue()).doubleValue() + c;
                    edge_scores.put(w_neighbors_entry.getValue(), e_score);
                }
                if (!w.equals(s)) {
                    Double w_score = vertex_scores.get(w).doubleValue() + vertex_data.get(w).delta;
                    vertex_scores.put(w, w_score);
                }
            }
        }

        if (normalize && graph instanceof UndirectedGraph || graph instanceof UndirectedHypergraph) {
            for (V v : graph.getVertices()) {
                double v_score = vertex_scores.get(v).doubleValue();
                v_score /= 2.0;
                vertex_scores.put(v, v_score);
            }
            for (E e : graph.getEdges()) {
                double e_score = edge_scores.get(e).doubleValue();
                e_score /= 2.0;
                edge_scores.put(e, e_score);
            }
        }

        vertex_data.clear();
    }

    private Multimap<V,E> getNeighbors(V v, Collection<E> vEdges) {
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

    private void initialize(Hypergraph<V, E> graph) {
        this.graph = graph;
        this.vertex_scores = new HashMap<V, Double>();
        this.edge_scores = new HashMap<E, Double>();
        this.vertex_data = new HashMap<V, BetweennessData>();

        for (V v : graph.getVertices())
            this.vertex_scores.put(v, 0.0);

        for (E e : graph.getEdges())
            this.edge_scores.put(e, 0.0);
    }

    private class BetweennessData {
        double distance; //d
        double pathCount; //sigma
        List<E> incomingEdges;
        List<V> predecessors;
        double delta;

        BetweennessData() {
            distance = -1;
            pathCount = 0;
            predecessors = new ArrayList<V>();
            incomingEdges = new ArrayList<E>();
            delta = 0;
        }

        @Override
        public String toString() {
            return "[distance:" + distance + ", pathCount:" + pathCount + ", predecessors:" + incomingEdges + ", delta:" + delta + "]\n";
        }
    }

    private class BetweennessComparator implements Comparator<V> {
        @Override
        public int compare(V v1, V v2) {
            return vertex_data.get(v1).distance > vertex_data.get(v2).distance ? 1 : -1;
        }
    }

    /**
     * Increasing comparator used for priority queues.
     */
    protected class BrandesNodeComparatorLargerFirst implements Comparator<V> {
        @Override
        public int compare(V x, V y) {
            double yy = vertex_data.get(y).distance;
            double xx = vertex_data.get(x).distance;
            if (xx > yy)
                return -1;
            else if (xx < yy)
                return 1;
            return 0;
        }
    }

    /**
     * Decreasing comparator used for priority queues.
     */
    protected class BrandesNodeComparatorSmallerFirst implements Comparator<V> {
        @Override
        public int compare(V x, V y) {
            double yy = vertex_data.get(y).distance;
            double xx = vertex_data.get(x).distance;
            if (xx > yy)
                return 1;
            else if (xx < yy)
                return -1;
            return 0;
        }
    }
}
