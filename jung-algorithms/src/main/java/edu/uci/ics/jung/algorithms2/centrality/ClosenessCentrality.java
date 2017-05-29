package edu.uci.ics.jung.algorithms2.centrality;

import com.google.common.base.Function;
import edu.uci.ics.jung.algorithms.scoring.VertexScorer;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.algorithms2.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.graph.Hypergraph;

import java.util.HashMap;
import java.util.Map;

public class ClosenessCentrality<V, E> implements VertexScorer<V, Double> {
    /**
     * The graph on which the vertex scores are to be calculated.
     */
    private Hypergraph<V, E> graph;

    /**
     * The metric to use for specifying the distance between pairs of vertices.
     */
    private Distance<V> distance;

    /**
     * The cache for the output results.  Null encodes "not yet calculated",
     * &lt; 0 encodes "no such distance exists".
     */
    private Map<V, Double> cache;

    /**
     * Specifies whether the values returned are the sum of the v-distances
     * or the mean v-distance.
     */
    private boolean averaging;

    /**
     * Specifies whether, for a vertex <code>v</code> with missing distances,
     * <code>v</code>'s score should be set to 'null' or '0'.
     * Defaults to 'true'.
     */
    private boolean null_infinitie_distances;

    /**
     * Specifies whether the values returned should ignore self-distances
     * (distances from <code>v</code> to itself).
     * Defaults to 'true'.
     */
    private boolean ignore_self_distances;

    /**
     * Creates an instance with the specified graph, distance metric, and
     * averaging behavior.
     *
     * @param graph                    The graph on which the vertex scores are to be calculated.
     * @param distance                 The metric to use for specifying the distance between
     *                                 pairs of vertices.
     * @param averaging                Specifies whether the values returned is the sum of all
     *                                 v-distances or the mean v-distance.
     * @param null_infinitie_distances Specifies whether scores for missing distances should
     *                                 be set to 'null' or '0'.
     * @param ignore_self_distances    Specifies whether distances from a vertex
     *                                 to itself should be included in its score.
     */
    public ClosenessCentrality(Hypergraph<V, E> graph,
                               Distance<V> distance,
                               boolean averaging,
                               boolean null_infinitie_distances,
                               boolean ignore_self_distances) {
        this.graph = graph;
        this.distance = distance;
        this.averaging = averaging;
        this.null_infinitie_distances = null_infinitie_distances;
        this.ignore_self_distances = ignore_self_distances;
        this.cache = new HashMap<V, Double>();
    }

    /**
     * Creates an instance with the specified graph and averaging behavior
     * whose vertex distances are calculated based on the specified edge
     * weights.
     *
     * @param graph                    The graph on which the vertex scores are to be
     *                                 calculated.
     * @param edge_weights             The edge weights to use for specifying the distance
     *                                 between pairs of vertices.
     * @param averaging                Specifies whether the values returned is the sum of
     *                                 all v-distances or the mean v-distance.
     * @param null_infinitie_distances Specifies whether scores for missing distances should
     *                                 be set to 'null' or '0'.
     * @param ignore_self_distances    Specifies whether distances from a vertex
     *                                 to itself should be included in its score.
     */
    public ClosenessCentrality(Hypergraph<V, E> graph,
                               Function<E, ? extends Number> edge_weights,
                               boolean averaging,
                               boolean null_infinitie_distances,
                               boolean ignore_self_distances) {
        this(graph, new DijkstraDistance<V, E>(graph, edge_weights), averaging, null_infinitie_distances, ignore_self_distances);
    }

    public ClosenessCentrality(Hypergraph<V, E> graph,
                               Function<E, ? extends Number> edge_weights,
                               boolean averaging,
                               boolean null_infinitie_distances) {
        this(graph, new DijkstraDistance<V, E>(graph, edge_weights), averaging, null_infinitie_distances, true);
    }

    /**
     * Creates an instance with the specified graph and averaging behavior
     * whose vertex distances are calculated on the unweighted graph.
     *
     * @param graph                    The graph on which the vertex scores are to be
     *                                 calculated.
     * @param averaging                Specifies whether the values returned is the sum of
     *                                 all v-distances or the mean v-distance.
     * @param null_infinitie_distances Specifies whether scores for missing distances should
     *                                 be set to 'null' or '0'.
     * @param ignore_self_distances    Specifies whether distances from a vertex
     *                                 to itself should be included in its score.
     */
    public ClosenessCentrality(Hypergraph<V, E> graph,
                               boolean averaging,
                               boolean null_infinitie_distances,
                               boolean ignore_self_distances) {
        this(graph, new UnweightedShortestPath<V, E>(graph), averaging, null_infinitie_distances, ignore_self_distances);
    }

    public ClosenessCentrality(Hypergraph<V, E> graph,
                               boolean averaging,
                               boolean null_infinitie_distances) {
        this(graph, new UnweightedShortestPath<V, E>(graph), averaging, null_infinitie_distances, true);
    }

    /**
     * Calculates the score for the specified vertex.  Returns {@code null} if
     * there are missing distances and such are not ignored by this instance.
     */
    @Override
    public Double getVertexScore(V v) {
        Double value = cache.get(v);
        if (value != null) {
            if (value < 0)
                return null;
            return value;
        }

        Map<V, Number> v_distances = new HashMap<V, Number>(distance.getDistanceMap(v));
        if (ignore_self_distances)
            v_distances.remove(v);

        if (v_distances.isEmpty()) {
            if (null_infinitie_distances) {
                cache.put(v, -1.0);
                return null;
            } else {
                cache.put(v, 0.0);
                return 0.0;
            }
        }

        Double sum = 0.0;
        for (V w : graph.getVertices()) {
            if (w.equals(v) && ignore_self_distances)
                continue;
            Number w_distance = v_distances.get(w);
            if (w_distance == null) {
                if (null_infinitie_distances) {
                    cache.put(v, -1.0);
                    return null;
                }
            } else {
                sum += w_distance.doubleValue();
            }
        }
        value = sum;
        if (averaging)
            value /= v_distances.size();

        double score = value == 0 ? Double.POSITIVE_INFINITY : 1 / value;
        cache.put(v, score);

        return score;
    }
}
