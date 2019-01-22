package edu.uci.ics.jung.algorithms2.conectivity;

import edu.uci.ics.jung.graph.Hypergraph;
import java.util.List;
import java.util.Set;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Base implementation of the strongly connected components algorithm.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
abstract class AbstractWeakConnectivity<V, E> implements Connectivity<V, E> {

    protected final Hypergraph<V, E> graph;
    protected List<Set<V>> connectedSets;
    protected List<Hypergraph<V, E>> connectedSubgraphs;

    public AbstractWeakConnectivity(Hypergraph<V, E> graph) {
        this.graph = graph;
    }

    @Override
    public Hypergraph<V, E> getGraph() {
        return graph;
    }

    //TODO
    @Override
    public List<Hypergraph<V, E>> getConnectedSubgraphs() {
        throw new NotImplementedException();
        // if (connectedSubgraphs == null) {
        //     List<Set<V>> sets = connectedSets();
        //     connectedSubgraphs = new ArrayList<>(sets.size());
        //
        //     for (Set<V> set : sets) {
        //         connectedSubgraphs.add(new AsSubgraph<>(graph, set, null));
        //     }
        // }
        // return connectedSubgraphs;
    }

}