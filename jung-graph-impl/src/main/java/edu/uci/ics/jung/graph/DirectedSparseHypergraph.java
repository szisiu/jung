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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

import java.util.*;

/**
 * An implementation of <code>Hypergraph</code> that is suitable for sparse graphs and does not permits parallel edges.
 */
@SuppressWarnings("serial")
public class DirectedSparseHypergraph<V, E> extends AbstractTypedHypergraph<V, E> implements DirectedHypergraph<V, E> {

    protected Map<V, Pair<Multimap<V, E>>> vertices; // Map of vertices to incident hyperedge sets {incoming, outgoing}
    protected Map<E, Pair<Set<V>>> edges;    // Map of hyperedges to incident vertex sets {head, tail}

    /**
     * Creates a <code>DirectedSparseHypergraph</code> and initializes the internal data structures.
     */
    public DirectedSparseHypergraph() {
        super(EdgeType.DIRECTED);
        vertices = new HashMap<V, Pair<Multimap<V, E>>>();
        edges = new HashMap<E, Pair<Set<V>>>();
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
                return new DirectedSparseHypergraph<V, E>();
            }
        };
    }

    /**
     * Adds <code>hyperedge</code> to this graph and connects them to the vertex collection <code>endpoints</code>.
     * Any vertices in <code>endpoints</code> that appear more than once will only appear once in the
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

        if (endpoints.size() == 2) { //src->dest
            Iterator<? extends V> it = endpoints.iterator();
            V v1 = it.next();
            V v2 = it.next();
            return addEdge(hyperedge, Sets.newHashSet(v1), Sets.newHashSet(v2), edgeType);
        } else if (endpoints.size() == 1) { //1 vertex loop
            V v = endpoints.iterator().next();
            Set<V> vset = new HashSet<V>();
            vset.add(v);
            return addEdge(hyperedge, vset, vset, edgeType);
        } else if(endpoints.size() > 2) { //n vertex loop
            Set<V> vs = Sets.newHashSet(endpoints);
            return addEdge(hyperedge, vs, vs, edgeType);
        } else {
            throw new IllegalArgumentException("Graph objects connect 1 or 2 vertices; vertices arg has " + endpoints.size());
        }
    }

    public boolean addEdge(E edge, Set<? extends V> source, Set<? extends V> dest) {
        return addEdge(edge, source, dest, this.getDefaultEdgeType());
    }

    public boolean addEdge(E hyperedge, Set<? extends V> source, Set<? extends V> dest, EdgeType edgeType) {
        this.validateEdgeType(edgeType);

        if (hyperedge == null)
            throw new IllegalArgumentException("input hyperedge may not be null");

        if (source == null || dest == null)
            throw new IllegalArgumentException("endpoints may not be null");

        if (source.contains(null) || source.contains(null))
            throw new IllegalArgumentException("cannot add an edge with a null endpoint");


        Set<V> sourceSet = new HashSet<V>(source);
        Set<V> destSet = new HashSet<V>(dest);

        if (edges.containsKey(hyperedge)) {
            Pair<Set<V>> attached = edges.get(hyperedge);
            if (!attached.getFirst().equals(sourceSet) || !attached.getSecond().equals(destSet)) {
                throw new IllegalArgumentException("Edge " + hyperedge + " exists in this graph with endpoints " + attached);
            } else
                return false;
        }

        Pair<Set<V>> new_endpoints = new Pair<Set<V>>(sourceSet, destSet);
        edges.put(hyperedge, new_endpoints);

        for (V s : sourceSet) {
            // add v if it's not already in the graph
            if (!vertices.containsKey(s))
                this.addVertex(s);
        }
        for (V d : destSet) {
            // add v if it's not already in the graph
            if (!vertices.containsKey(d))
                this.addVertex(d);
        }

        // map source of this edge to <dest, edge> and vice versa
        for (V s : sourceSet) {
            Multimap<V, E> sourceOut = vertices.get(s).getSecond();
            for (V d : destSet) {
                sourceOut.put(d, hyperedge);
            }
        }
        for (V d : destSet) {
            Multimap<V, E> destIn = vertices.get(d).getFirst();
            for (V s : sourceSet) {
                destIn.put(s, hyperedge);
            }
        }
        return true;
    }

    @Override
    public boolean addVertex(V vertex) {
        if (vertex == null)
            throw new IllegalArgumentException("cannot add a null vertex");
        if (containsVertex(vertex))
            return false;
        Multimap<V, E> in = HashMultimap.create();
        Multimap<V, E> out = HashMultimap.create();
        vertices.put(vertex, new Pair<Multimap<V, E>>(in, out));
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

    @Deprecated
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

        Pair<Set<V>> tmp = edges.get(edge);
        Set<V> endpoints = Sets.newHashSet();
        endpoints.addAll(tmp.getFirst());
        endpoints.addAll(tmp.getSecond());
        return Collections.unmodifiableCollection(endpoints);
    }

    @Override
    public Collection<V> getOpposite(V vertex, E edge)
    {
        if (!containsEdge(edge))
            return null;

        Pair<Set<V>> endpoints = edges.get(edge);

        boolean found = true;
        if(!endpoints.getFirst().contains(vertex)) {
            found = false;
        } else {
            return endpoints.getSecond();
        }
        if(!endpoints.getSecond().contains(vertex) && !found) {
            throw new IllegalArgumentException(vertex + " is not incident to " + edge + " in this graph");
        } else {
            return endpoints.getFirst();
        }
    }

    public Collection<V> getSourceSet(E edge) {
        if (!containsEdge(edge))
            return null;
        return edges.get(edge).getFirst();
    }

    public Collection<V> getDestSet(E edge) {
        if (!containsEdge(edge))
            return null;
        return edges.get(edge).getSecond();
    }

    @Override
    public Collection<E> getInEdges(V vertex) {
        if (!containsVertex(vertex))
            return null;
        return Collections.unmodifiableCollection(getIncoming_internal(vertex));
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

        Set<E> incident_edges = new HashSet<E>();
        incident_edges.addAll(getIncoming_internal(vertex));
        incident_edges.addAll(getOutgoing_internal(vertex));
        return Collections.unmodifiableCollection(incident_edges);
    }

    protected Collection<E> getIncoming_internal(V vertex) {
        return Sets.newHashSet(vertices.get(vertex).getFirst().values());
    }

    @Override
    public Collection<V> getNeighbors(V vertex) {
        if (!containsVertex(vertex))
            return null;

        Set<V> neighbors = Sets.newHashSet();
        neighbors.addAll(getPreds_internal(vertex));
        neighbors.addAll(getSuccs_internal(vertex));
        return Collections.unmodifiableCollection(neighbors);
    }

    @Override
    public Collection<E> getOutEdges(V vertex) {
        if (!containsVertex(vertex))
            return null;
        return Collections.unmodifiableCollection(getOutgoing_internal(vertex));
    }

    protected Collection<E> getOutgoing_internal(V vertex) {
        return Sets.newHashSet(vertices.get(vertex).getSecond().values());
    }

    @Override
    public Collection<V> getPredecessors(V vertex) {
        if (!containsVertex(vertex))
            return null;
        return Collections.unmodifiableCollection(getPreds_internal(vertex));
    }

    protected Collection<V> getPreds_internal(V vertex) {
        return Sets.newHashSet(vertices.get(vertex).getFirst().keySet());
    }

    @Deprecated
    @Override
    public V getSource(E directed_edge) {
        return null;
    }

    @Override
    public Collection<V> getSuccessors(V vertex) {
        if (!containsVertex(vertex))
            return null;
        return Collections.unmodifiableCollection(getSuccs_internal(vertex));
    }

    protected Collection<V> getSuccs_internal(V vertex) {
        return Sets.newHashSet(vertices.get(vertex).getSecond().keySet());
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
    public boolean removeEdge(E edge) {
        if (!containsEdge(edge))
            return false;

        Collection<V> sourceSet = getSourceSet(edge);
        Collection<V> destSet = getDestSet(edge);

        // remove vertices from each others' adjacency maps
        for (V source : sourceSet) {
            Multimap<V, E> sourceOut = vertices.get(source).getSecond();
            for (V dest : destSet) {
                sourceOut.remove(dest, edge);
            }
        }
        for (V dest : destSet) {
            Multimap<V, E> destIn = vertices.get(dest).getFirst();
            for (V source : sourceSet) {
                destIn.remove(source, edge);
            }
        }

        edges.remove(edge);
        return true;
    }

    @Override
    public boolean removeVertex(V vertex) {
        if (!containsVertex(vertex))
            return false;

        // copy to avoid concurrent modification in removeEdge
        ArrayList<E> incident = new ArrayList<E>(getIncoming_internal(vertex));
        incident.addAll(getOutgoing_internal(vertex));

        for (E edge : incident)
            removeEdge(edge);

        vertices.remove(vertex);

        return true;
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
            Collection<V> sourceSet = getSourceSet(e);
            Collection<V> destSet = getDestSet(e);
            sb.append(e + "" + Arrays.deepToString(sourceSet.toArray()) + " -> " + Arrays.deepToString(destSet.toArray()) + "\n");
        }
        return sb.toString();
    }
}
