package edu.uci.ics.jung.algorithms2.conectivity;

import edu.uci.ics.jung.graph.Hypergraph;
import java.util.List;
import java.util.Set;

public interface Connectivity<V, E>
{
    /**
     * Return the underlying graph.
     *
     * @return the underlying graph
     */
    Hypergraph<V, E> getGraph();

    /**
     * Returns true if the graph is connected, false otherwise.
     *
     * @return true if the graph is connected, false otherwise
     */
    default boolean isConnected() {
        return getConnectedSets().size() == 1;
    }

    /**
     * Computes a {@link List} of {@link Set}s, where each set contains vertices which together form
     * a connected component within the given graph.
     *
     * @return <code>List</code> of <code>Set</code> s containing the connected components
     */
    List<Set<V>> getConnectedSets();

    /**
     * Computes a list of subgraphs of the given graph. Each subgraph will represent a
     * connected component and will contain all vertices of that component. The subgraph will have
     * an edge $(u,v)$ iff $u$ and $v$ are contained in the  connected component.
     *
     * @return a list of subgraphs representing the connected components
     */
    List<Hypergraph<V, E>> getConnectedSubgraphs();

}