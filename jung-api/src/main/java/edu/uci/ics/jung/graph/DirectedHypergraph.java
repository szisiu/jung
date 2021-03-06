/*
 * Created on Oct 17, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.graph;

/**
 * A tagging interface for implementations of <code>Hypergraph</code>
 * that accept only directed edges.
 *
 * @author Tom Nelson - tomnelson@dev.java.net
 *
 * @param <V>   type specification for vertices
 * @param <H>   type specification for edges
 */
public interface DirectedHypergraph<V,H> extends Hypergraph<V,H> {
}
