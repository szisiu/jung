package edu.uci.ics.jung.algorithms2.shortestpath;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Algorithm class which computes a number of distance related metrics. A summary of various
 * distance metrics can be found
 * <a href="https://en.wikipedia.org/wiki/Distance_(graph_theory)">here</a>.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable, Alexandru Valeanu
 */
public class GraphDistance<V, E>
{

    /* Input graph */
    private final Hypergraph<V, E> graph;
    /* All-pairs shortest path algorithm */
    private final Distance<V> distance;

    /* Vertex eccentricity map */
    private Map<V, Double> eccentricityMap = null;
    /* Diameter of the graph */
    private double diameter = 0;
    /* Radius of the graph */
    private double radius = Double.POSITIVE_INFINITY;

    /**
     * Constructs a new instance of GraphMeasurer. {@link DijkstraDistance} is used with all edge weights=1 as the
     * default shortest path algorithm.
     *
     * @param graph input graph
     */
    public GraphDistance(Graph<V, E> graph)
    {
        this(graph, new DijkstraDistance<V, E>(graph, Functions.<Double>constant(1.0)));
    }

    /**
     * Constructs a new instance of GraphMeasurer. {@link DijkstraDistance} is used as the
     * default shortest path algorithm.
     *
     * @param graph input graph
     */
    public GraphDistance(Graph<V, E> graph, Function<E, ? extends Number> edge_weights)
    {
        this(graph, new DijkstraDistance<V, E>(graph, edge_weights));
    }

    /**
     * Constructs a new instance of GraphMeasurer.
     * 
     * @param graph input graph
     * @param distance shortest path algorithm used to compute shortest paths between
     *        all pairs of vertices.
     */
    public GraphDistance(Graph<V, E> graph, Distance<V> distance)
    {
        this.graph = graph;
        this.distance = distance;
    }

    /**
     * Compute the <a href="http://mathworld.wolfram.com/GraphDiameter.html">diameter</a> of the
     * graph. The diameter of a graph is defined as $\max_{v\in V}\epsilon(v)$, where $\epsilon(v)$
     * is the eccentricity of vertex $v$. In other words, this method computes the 'longest shortest
     * path'. Two special cases exist. If the graph has no vertices, the diameter is 0. If the graph
     * is disconnected, the diameter is {@link Double#POSITIVE_INFINITY}.
     *
     * @return the diameter of the graph.
     */
    public double getDiameter()
    {
        computeEccentricityMap();
        return diameter;
    }

    /**
     * Compute the <a href="http://mathworld.wolfram.com/GraphRadius.html">radius</a> of the graph.
     * The radius of a graph is defined as $\min_{v\in V}\epsilon(v)$, where $\epsilon(v)$ is the
     * eccentricity of vertex $v$. Two special cases exist. If the graph has no vertices, the radius
     * is 0. If the graph is disconnected, the diameter is {@link Double#POSITIVE_INFINITY}.
     *
     * @return the diameter of the graph.
     */
    public double getRadius()
    {
        computeEccentricityMap();
        return radius;
    }

    /**
     * Compute the <a href="http://mathworld.wolfram.com/GraphEccentricity.html">eccentricity</a> of
     * each vertex in the graph. The eccentricity of a vertex $u$ is defined as $\max_{v}d(u,v)$,
     * where $d(u,v)$ is the shortest path between vertices $u$ and $v$. If the graph is
     * disconnected, the eccentricity of each vertex is {@link Double#POSITIVE_INFINITY}. The
     * runtime complexity of this method is $O(n^2+L)$, where $L$ is the runtime complexity of the
     * shortest path algorithm provided during construction of this class.
     *
     * @return a map containing the eccentricity of each vertex.
     */
    public Map<V, Double> getVertexEccentricityMap()
    {
        computeEccentricityMap();
        return Collections.unmodifiableMap(this.eccentricityMap);
    }

    /**
     * Compute the <a href="http://mathworld.wolfram.com/GraphCenter.html">graph center</a>. The
     * center of a graph is the set of vertices of graph eccentricity equal to the graph radius.
     *
     * @return the graph center
     */
    public Set<V> getGraphCenter()
    {
        computeEccentricityMap();
        Set<V> graphCenter = new LinkedHashSet<>();
        ToleranceDoubleComparator comp = new ToleranceDoubleComparator();
        for (Map.Entry<V, Double> entry : eccentricityMap.entrySet()) {
            if (comp.compare(entry.getValue(), radius) == 0) {
                graphCenter.add(entry.getKey());
            }
        }
        return graphCenter;
    }

    /**
     * Compute the <a href="http://mathworld.wolfram.com/GraphPeriphery.html">graph periphery</a>.
     * The periphery of a graph is the set of vertices of graph eccentricity equal to the graph
     * diameter.
     * 
     * @return the graph periphery
     */
    public Set<V> getGraphPeriphery()
    {
        computeEccentricityMap();
        Set<V> graphPeriphery = new LinkedHashSet<>();
        ToleranceDoubleComparator comp = new ToleranceDoubleComparator();
        for (Map.Entry<V, Double> entry : eccentricityMap.entrySet()) {
            if (comp.compare(entry.getValue(), diameter) == 0) {
                graphPeriphery.add(entry.getKey());
            }
        }
        return graphPeriphery;
    }

    /**
     * Compute the graph pseudo-periphery. The pseudo-periphery of a graph is the set of all
     * pseudo-peripheral vertices. A pseudo-peripheral vertex $v$ has the property that for any
     * vertex $u$, if $v$ is as far away from $u$ as possible, then $u$ is as far away from $v$ as
     * possible. Formally, a vertex $u$ is pseudo-peripheral, if for each vertex $v$ with
     * $d(u,v)=\epsilon(u)$ holds $\epsilon(u)=\epsilon(v)$, where $\epsilon(u)$ is the eccentricity
     * of vertex $u$.
     *
     * @return the graph pseudo-periphery
     */
    public Set<V> getGraphPseudoPeriphery()
    {
        computeEccentricityMap();
        Set<V> graphPseudoPeriphery = new LinkedHashSet<>();
        ToleranceDoubleComparator comp = new ToleranceDoubleComparator();

        for (Map.Entry<V, Double> entry : eccentricityMap.entrySet()) {
            V u = entry.getKey();

            for (V v : graph.getVertices()) {
                if (comp.compare(distance.getDistance(u, v).doubleValue(), entry.getValue()) == 0
                    && comp.compare(entry.getValue(), eccentricityMap.get(v)) == 0) {
                    graphPseudoPeriphery.add(entry.getKey());
                }
            }
        }

        return graphPseudoPeriphery;
    }

    /**
     * Lazy method which computes the eccentricity of each vertex
     */
    private void computeEccentricityMap()
    {
        if (eccentricityMap != null) {
            return;
        }

        // Compute the eccentricity map
        eccentricityMap = new LinkedHashMap<>();
        if (graph.getDefaultEdgeType().equals(EdgeType.DIRECTED)) {
            List<V> vertices = new ArrayList<>(graph.getVertices());
            double[] eccentricityVector = new double[vertices.size()];
            for (int i = 0; i < vertices.size() - 1; i++) {
                for (int j = i + 1; j < vertices.size(); j++) {
                    Double dist = distance.getDistance(vertices.get(i), vertices.get(j)).doubleValue();
                    eccentricityVector[i] = Math.max(eccentricityVector[i], dist);
                    eccentricityVector[j] = Math.max(eccentricityVector[j], dist);
                }
            }
            for (int i = 0; i < vertices.size(); i++) {
                eccentricityMap.put(vertices.get(i), eccentricityVector[i]);
            }
        } else {
            for (V u : graph.getVertices()) {
                double eccentricity = 0;
                for (V v : graph.getVertices()) {
                    eccentricity =
                        Double.max(eccentricity, distance.getDistance(u, v).doubleValue());
                }
                eccentricityMap.put(u, eccentricity);
            }
        }

        // Compute the graph diameter and radius
        if (eccentricityMap.isEmpty()) {
            diameter = 0;
            radius = 0;
        } else {
            for (V v : graph.getVertices()) {
                diameter = Math.max(diameter, eccentricityMap.get(v));
                radius = Math.min(radius, eccentricityMap.get(v));
            }
        }
    }

    /**
     * A double comparator with adjustable tolerance.
     *
     * @author Dimitrios Michail
     */
    class ToleranceDoubleComparator implements Comparator<Double>
    {
        /**
         * Default tolerance used by the comparator.
         */
        public static final double DEFAULT_EPSILON = 1e-9;

        private final double epsilon;

        /**
         * Construct a new comparator with a {@link #DEFAULT_EPSILON} tolerance.
         */
        public ToleranceDoubleComparator()
        {
            this(DEFAULT_EPSILON);
        }

        /**
         * Construct a new comparator with a specified tolerance.
         *
         * @param epsilon the tolerance
         */
        public ToleranceDoubleComparator(double epsilon)
        {
            if (epsilon <= 0.0) {
                throw new IllegalArgumentException("Tolerance must be positive");
            }
            this.epsilon = epsilon;
        }

        /**
         * Compares two floating point values. Returns 0 if they are equal, -1 if {@literal o1 < o2}, 1
         * otherwise
         *
         * @param o1 the first value
         * @param o2 the second value
         * @return 0 if they are equal, -1 if {@literal o1 < o2}, 1 otherwise
         */
        @Override
        public int compare(Double o1, Double o2)
        {
            if (Math.abs(o1 - o2) < epsilon) {
                return 0;
            } else {
                return Double.compare(o1, o2);
            }
        }
    }
}