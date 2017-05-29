/**
 * Copyright (c) 2008, The JUNG Authors
 * <p>
 * All rights reserved.
 * <p>
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * Created on Sep 1, 2008
 */
package edu.uci.ics.jung.graph;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * An abstract class for graphs whose edges all have the same {@code EdgeType}.
 * Intended to simplify the implementation of such graph classes.
 */
@SuppressWarnings("serial")
public abstract class AbstractHypergraph<V, E> implements Hypergraph<V, E>, Serializable {

    @Override
    public boolean addEdge(E edge, Collection<? extends V> vertices) {
        return addEdge(edge, vertices, this.getDefaultEdgeType());
    }

    @Override
    public int degree(V vertex) {
        if (!containsVertex(vertex))
            throw new IllegalArgumentException(vertex + " is not a vertex in this graph");
        return this.getIncidentEdges(vertex).size();
    }

    public abstract Collection<V> getEndpoints(E edge);

    public abstract Collection<V> getOpposite(V vertex, E edge);

    @Override
    public Collection<V> getIncidentVertices(E edge) {
        return Collections.unmodifiableCollection(this.getEndpoints(edge));
    }

    @Override
    public int getNeighborCount(V vertex) {
        if (!containsVertex(vertex))
            return 0;

        return getNeighbors(vertex).size();
    }

    @Override
    public int inDegree(V vertex) {
        return this.getInEdges(vertex).size();
    }

    @Override
    public boolean isIncident(V vertex, E edge) {
        if (!containsVertex(vertex) || !containsEdge(edge))
            throw new IllegalArgumentException("At least one of these not in this graph: " + vertex + ", " + edge);
        return this.getIncidentEdges(vertex).contains(edge);
    }

    @Override
    public boolean isNeighbor(V v1, V v2) {
        if (!containsVertex(v1) || !containsVertex(v2))
            throw new IllegalArgumentException("At least one of these not in this graph: " + v1 + ", " + v2);
        return this.getNeighbors(v1).contains(v2);
    }

    @Override
    public int outDegree(V vertex) {
        return this.getOutEdges(vertex).size();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("Vertices:\n");
        for (V v : getVertices()) {
            sb.append(v + ",");
        }
        sb.setLength(sb.length() - 1);
        sb.append("\nEdges:\n");
        for (E e : getEdges()) {
            Collection<V> ep = getEndpoints(e);
            sb.append(e + "" + Arrays.deepToString(ep.toArray()) + "\n");
        }
        return sb.toString();
    }
}
