/*
 * Created on Feb 4, 2007
 *
 * Copyright (c) 2007, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.graph;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.util.*;

/**
 * An implementation of <code>Hypergraph</code> that is suitable for sparse hypergraphs and does not permits parallel edges.
 */
@SuppressWarnings("serial")
public class UndirectedSparseHypergraph<V, E> extends AbstractTypedHypergraph<V, E> implements UndirectedHypergraph<V, E> {

    protected Map<V, Set<E>> vertices; // Map of vertices to incident hyperedge sets
    protected Map<E, Set<V>> edges;    // Map of hyperedges to incident vertex sets

    /**
     * Creates a <code>UndirectedSparseHypergraph</code> and initializes the internal data structures.
     */
    public UndirectedSparseHypergraph() {
        super(EdgeType.UNDIRECTED);
        vertices = new HashMap<V, Set<E>>();
        edges = new HashMap<E, Set<V>>();
    }

    /**
     * Returns a <code>Factory</code> which creates instances of this class.
     *
     * @param <V> vertex type of the hypergraph to be created
     * @param <E> edge type of the hypergraph to be created
     * @return a <code>Factory</code> which creates instances of this class
     */
    public static <V, E> Supplier<Hypergraph<V, E>> getFactory() {
        return new Supplier<Hypergraph<V, E>>() {
            @Override
            public Hypergraph<V, E> get() {
                return new UndirectedSparseHypergraph<V, E>();
            }
        };
    }

    /**
     * Adds <code>hyperedge</code> to this graph and connects them to the vertex collection <code>endpoints</code>.
     * Any endpoints in <code>endpoints</code> that appear more than once will only appear once in the
     * incident vertex collection for <code>hyperedge</code>, that is, duplicates will be ignored.
     *
     * @see Hypergraph#addEdge(Object, Collection)
     */
    @Override
    public boolean addEdge(E hyperedge, Collection<? extends V> endpoints, EdgeType edgeType) {
        this.validateEdgeType(edgeType);

        if (hyperedge == null)
            throw new IllegalArgumentException("input hyperedge may not be null");

        if (endpoints == null)
            throw new IllegalArgumentException("endpoints may not be null");

        if (endpoints.contains(null))
            throw new IllegalArgumentException("cannot add an edge with a null endpoint");

        Set<V> new_endpoints = new HashSet<V>(endpoints);
        if (edges.containsKey(hyperedge)) {
            Collection<V> attached = edges.get(hyperedge);
            if (!attached.equals(new_endpoints)) {
                throw new IllegalArgumentException("Edge " + hyperedge + " exists in this graph with endpoints " + attached);
            } else
                return false;
        }

        edges.put(hyperedge, new_endpoints);
        for (V v : endpoints) {
            // add v if it's not already in the graph
            addVertex(v);

            // associate v with hyperedge
            vertices.get(v).add(hyperedge);
        }
        return true;
    }

    @Override
    public boolean addVertex(V vertex) {
        if (vertex == null)
            throw new IllegalArgumentException("cannot add a null vertex");
        if (containsVertex(vertex))
            return false;
        vertices.put(vertex, new HashSet<E>());
        return true;
    }

    @Override
    public boolean containsEdge(E edge) {
        return edges.keySet().contains(edge);
    }

    @Override
    public boolean containsVertex(V vertex) {
        return vertices.keySet().contains(vertex);
    }

    @Override
    public E findEdge(V v1, V v2) {
        if (!containsVertex(v1) || !containsVertex(v2))
            return null;

        for (E h : getIncidentEdges(v1)) {
            if (isIncident(v2, h))
                return h;
        }
        return null;
    }

    @Override
    public Collection<E> findEdgeSet(V v1, V v2) {
        if (!containsVertex(v1) || !containsVertex(v2))
            return null;

        Collection<E> edges = new ArrayList<E>();
        for (E h : getIncidentEdges(v1)) {
            if (isIncident(v2, h))
                edges.add(h);
        }
        return Collections.unmodifiableCollection(edges);
    }

    @Override
    public V getDest(E directed_edge) {
        return null;
    }

    @Override
    public int getEdgeCount() {
        return edges.size();
    }

    @Override
    public Collection<E> getEdges() {
        return Collections.unmodifiableCollection(edges.keySet());
    }

    @Override
    public Collection<V> getEndpoints(E edge) {
        if (!containsEdge(edge))
            return null;
        return Collections.unmodifiableCollection(edges.get(edge));
    }

    @Override
    public Collection<V> getOpposite(V vertex, E edge)
    {
        if (!containsEdge(edge))
            return null;
        Collection<V> incident = Sets.newHashSet(edges.get(edge));
        if(incident.contains(vertex)) {
            incident.remove(vertex);
        } else {
            throw new IllegalArgumentException(vertex + " is not incident to " + edge + " in this graph");
        }
        return incident;
    }

    @Override
    public Collection<E> getInEdges(V vertex) {
        if (!containsVertex(vertex))
            return null;
        return getIncidentEdges(vertex);
    }

    @Override
    public int getIncidentCount(E edge) {
        if (!containsEdge(edge))
            return 0;

        return edges.get(edge).size();
    }

    @Override
    public Collection<E> getIncidentEdges(V vertex) {
        if (!containsVertex(vertex))
            return null;
        return Collections.unmodifiableCollection(vertices.get(vertex));
    }

    @Override
    public Collection<V> getNeighbors(V vertex) {
        if (!containsVertex(vertex))
            return null;

        Set<V> neighbors = new HashSet<V>();
        for (E hyperedge : vertices.get(vertex)) {
            neighbors.addAll(edges.get(hyperedge));
        }
        return Collections.unmodifiableCollection(neighbors);
    }

    @Override
    public Collection<E> getOutEdges(V vertex) {
        if (!containsVertex(vertex))
            return null;
        return getIncidentEdges(vertex);
    }

    @Override
    public Collection<V> getPredecessors(V vertex) {
        return getNeighbors(vertex);
    }

    @Override
    public V getSource(E directed_edge) {
        return null;
    }

    @Override
    public Collection<V> getSuccessors(V vertex) {
        return getNeighbors(vertex);
    }

    @Override
    public int getVertexCount() {
        return vertices.size();
    }

    @Override
    public Collection<V> getVertices() {
        return vertices.keySet();
    }

    @Override
    public boolean isIncident(V vertex, E edge) {
        if (!containsVertex(vertex) || !containsEdge(edge))
            return false;

        return vertices.get(vertex).contains(edge);
    }

    @Override
    public boolean removeEdge(E hyperedge) {
        if (!containsEdge(hyperedge))
            return false;
        for (V vertex : edges.get(hyperedge)) {
            vertices.get(vertex).remove(hyperedge);
        }
        edges.remove(hyperedge);
        return true;
    }

    @Override
    public boolean removeVertex(V vertex) {
        if (!containsVertex(vertex))
            return false;
        for (E hyperedge : vertices.get(vertex)) {
            edges.get(hyperedge).remove(vertex);
        }
        vertices.remove(vertex);
        return true;
    }
}
