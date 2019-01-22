package edu.uci.ics.jung.algorithms2.conectivity;

import edu.uci.ics.jung.graph.Hypergraph;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Finds all weak components in a graph as sets of vertex sets.  A weak component is defined as
 * a maximal subgraph in which all pairs of vertices in the subgraph are reachable from one
 * another in the underlying undirected subgraph.
 * <p>This implementation identifies components as sets of vertex sets.
 * To create the induced graphs from any or all of these vertex sets,
 * see <code>algorithms.filters.FilterUtils</code>.
 * <p>
 * Running time: O(|V| + |E|) where |V| is the number of vertices and |E| is the number of edges.
 */
public class WeakConectivity<V,E> extends AbstractWeakConnectivity<V, E> {

    /**
     * Constructor
     *
     * @param graph the graph to inspect
     * @throws NullPointerException in case the graph is null
     */
    public WeakConectivity(Hypergraph<V, E> graph) {
        super(graph);
    }

    @Override
    public List<Set<V>> getConnectedSets() {
        if (connectedSets == null) {
            connectedSets = new LinkedList<>();

            Set<V> unvisitedVertices = new HashSet<>(graph.getVertices());

            while (!unvisitedVertices.isEmpty()) {
                Set<V> cluster = new HashSet<>();
                V root = unvisitedVertices.iterator().next();
                unvisitedVertices.remove(root);
                cluster.add(root);

                Queue<V> queue = new LinkedList<>();
                queue.add(root);

                while (!queue.isEmpty()) {
                    V currentVertex = queue.remove();
                    Collection<V> neighbors = graph.getNeighbors(currentVertex);

                    for(V neighbor : neighbors) {
                        if (unvisitedVertices.contains(neighbor)) {
                            queue.add(neighbor);
                            unvisitedVertices.remove(neighbor);
                            cluster.add(neighbor);
                        }
                    }
                }
                connectedSets.add(cluster);
            }
        }

        return connectedSets;
    }
}
