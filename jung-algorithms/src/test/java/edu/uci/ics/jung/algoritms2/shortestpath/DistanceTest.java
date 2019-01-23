package edu.uci.ics.jung.algoritms2.shortestpath;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Sets;
import edu.uci.ics.jung.algorithms2.shortestpath.GraphDistance;
import edu.uci.ics.jung.graph.util.TestGraphs;
import org.junit.Before;
import org.junit.Test;

public class DistanceTest {

    private final static double EPSILON = 0.000000001;
    private GraphDistance<String, Number> distance;

    @Before
    public void setUp() {
        this.distance = new GraphDistance<>(TestGraphs.createChainPlusIsolates(5, 0));
    }

    @Test
    public void testGraphDiameter() {
        assertEquals(4.0, distance.getDiameter(), EPSILON);

    }

    @Test
    public void testGraphRadius() {
        assertEquals(2.0, distance.getRadius(), EPSILON);
    }

    @Test
    public void testGraphPeriphery() {
        assertEquals(Sets.newHashSet("v0", "v4"), distance.getGraphPeriphery());
    }
}
