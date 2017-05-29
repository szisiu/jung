package edu.uci.ics.jung.algorithms2.centrality;

import com.google.common.base.Function;
import edu.uci.ics.jung.algorithms.scoring.VertexScorer;
import edu.uci.ics.jung.graph.Hypergraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DegreeCentrality<V, E> implements VertexScorer<V, Double> {

    /**
     * The graph on which the vertex scores are to be calculated.
     */
    private Hypergraph<V, E> graph;

    private Function<E, Double> edgeWeightFn;

    private CentralityMode mode;

    private boolean normalize;

    /**
     * The cache for the output results.  Null encodes "not yet calculated",
     * &lt; 0 encodes "no such distance exists".
     */
    private Map<V, Double> cache;



    public DegreeCentrality(Hypergraph<V, E> graph, CentralityMode mode, boolean normalize, Function<E, Double> edgeWeightFn) {
        this.graph = graph;
        this.mode = mode;
        this.edgeWeightFn = edgeWeightFn;
        this.normalize = normalize;
        this.cache = new HashMap<V, Double>();
    }

    public DegreeCentrality(Hypergraph<V, E> graph, CentralityMode mode, Function<E, Double> edgeWeightFn) {
        this(graph, mode, false, edgeWeightFn);
    }

    public DegreeCentrality(Hypergraph<V, E> graph, CentralityMode mode, boolean normalize) {
        this(graph, mode, normalize, null);
    }

    public DegreeCentrality(Hypergraph<V, E> graph, CentralityMode mode) {
        this(graph, mode, false, null);
    }

    public DegreeCentrality(Hypergraph<V, E> graph) {
        this(graph, CentralityMode.TOTAL, false, null);
    }

    public Function<E, Double> getEdgeWeightFn() {
        return edgeWeightFn;
    }

    public void setEdgeWeightFn(Function<E, Double> edgeWeightFn) {
        this.edgeWeightFn = edgeWeightFn;
    }

    public CentralityMode getMode() {
        return mode;
    }

    public void setMode(CentralityMode mode) {
        this.mode = mode;
    }

    @Override
    public Double getVertexScore(V v) {
        Double score = cache.get(v);
        if (score != null) {
            return score;
        }

        Collection<E> adjEdges = null;
        switch (mode) {
            case IN:
                adjEdges = graph.getInEdges(v);
                break;
            case OUT:
                adjEdges = graph.getOutEdges(v);
                break;
            case TOTAL:
                adjEdges = graph.getIncidentEdges(v);
                break;
        }

        if (null != edgeWeightFn) {
            score = adjEdges.stream().mapToDouble(e -> edgeWeightFn.apply(e)).sum();
        } else {
            score = Double.valueOf(adjEdges.size());
        }

        if(normalize) {
            score /= (graph.getVertexCount() - 1);
        }

        cache.put(v, score);

        return score;
    }
}
