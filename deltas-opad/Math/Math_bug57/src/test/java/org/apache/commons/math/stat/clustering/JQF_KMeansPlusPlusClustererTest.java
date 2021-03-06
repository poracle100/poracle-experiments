/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math.stat.clustering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import com.pholser.junit.quickcheck.*;
import com.pholser.junit.quickcheck.generator.*;
import edu.berkeley.cs.jqf.fuzz.*;
import com.pholser.junit.quickcheck.random.*;
import anonymous.log.Log;
import com.thoughtworks.xstream.XStream;

@RunWith(JQF.class)
public class JQF_KMeansPlusPlusClustererTest {


    @Test
    public void dimension2() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer =
            new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(new Random(1746432956321l));
        EuclideanIntegerPoint[] points = new EuclideanIntegerPoint[] {

                // first expected cluster
                new EuclideanIntegerPoint(new int[] { -15,  3 }),
                new EuclideanIntegerPoint(new int[] { -15,  4 }),
                new EuclideanIntegerPoint(new int[] { -15,  5 }),
                new EuclideanIntegerPoint(new int[] { -14,  3 }),
                new EuclideanIntegerPoint(new int[] { -14,  5 }),
                new EuclideanIntegerPoint(new int[] { -13,  3 }),
                new EuclideanIntegerPoint(new int[] { -13,  4 }),
                new EuclideanIntegerPoint(new int[] { -13,  5 }),

                // second expected cluster
                new EuclideanIntegerPoint(new int[] { -1,  0 }),
                new EuclideanIntegerPoint(new int[] { -1, -1 }),
                new EuclideanIntegerPoint(new int[] {  0, -1 }),
                new EuclideanIntegerPoint(new int[] {  1, -1 }),
                new EuclideanIntegerPoint(new int[] {  1, -2 }),

                // third expected cluster
                new EuclideanIntegerPoint(new int[] { 13,  3 }),
                new EuclideanIntegerPoint(new int[] { 13,  4 }),
                new EuclideanIntegerPoint(new int[] { 14,  4 }),
                new EuclideanIntegerPoint(new int[] { 14,  7 }),
                new EuclideanIntegerPoint(new int[] { 16,  5 }),
                new EuclideanIntegerPoint(new int[] { 16,  6 }),
                new EuclideanIntegerPoint(new int[] { 17,  4 }),
                new EuclideanIntegerPoint(new int[] { 17,  7 })

        };
        List<Cluster<EuclideanIntegerPoint>> clusters =
            transformer.cluster(Arrays.asList(points), 3, 10);

        assertEquals(3, clusters.size());
        boolean cluster1Found = false;
        boolean cluster2Found = false;
        boolean cluster3Found = false;
        for (Cluster<EuclideanIntegerPoint> cluster : clusters) {
            int[] center = cluster.getCenter().getPoint();
            if (center[0] < 0) {
                cluster1Found = true;
                assertEquals(8, cluster.getPoints().size());
                assertEquals(-14, center[0]);
                assertEquals( 4, center[1]);
            } else if (center[1] < 0) {
                cluster2Found = true;
                assertEquals(5, cluster.getPoints().size());
                assertEquals( 0, center[0]);
                assertEquals(-1, center[1]);
            } else {
                cluster3Found = true;
                assertEquals(8, cluster.getPoints().size());
                assertEquals(15, center[0]);
                assertEquals(5, center[1]);
            }
        }
        assertTrue(cluster1Found);
        assertTrue(cluster2Found);
        assertTrue(cluster3Found);

    }

    /**
     * JIRA: MATH-305
     *
     * Two points, one cluster, one iteration
     */
    @Test
    public void testPerformClusterAnalysisDegenerate() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer = new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(
                new Random(1746432956321l));
        EuclideanIntegerPoint[] points = new EuclideanIntegerPoint[] {
                new EuclideanIntegerPoint(new int[] { 1959, 325100 }),
                new EuclideanIntegerPoint(new int[] { 1960, 373200 }), };
        List<Cluster<EuclideanIntegerPoint>> clusters = transformer.cluster(Arrays.asList(points), 1, 1);
        assertEquals(1, clusters.size());
        assertEquals(2, (clusters.get(0).getPoints().size()));
        EuclideanIntegerPoint pt1 = new EuclideanIntegerPoint(new int[] { 1959, 325100 });
        EuclideanIntegerPoint pt2 = new EuclideanIntegerPoint(new int[] { 1960, 373200 });
        assertTrue(clusters.get(0).getPoints().contains(pt1));
        assertTrue(clusters.get(0).getPoints().contains(pt2));

    }

    @Test
    public void testCertainSpace() {
        KMeansPlusPlusClusterer.EmptyClusterStrategy[] strategies = {
            KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE,
            KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_POINTS_NUMBER,
            KMeansPlusPlusClusterer.EmptyClusterStrategy.FARTHEST_POINT
        };
        for (KMeansPlusPlusClusterer.EmptyClusterStrategy strategy : strategies) {
            KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer =
                new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(new Random(1746432956321l), strategy);
            int numberOfVariables = 27;
            // initialise testvalues
            int position1 = 1;
            int position2 = position1 + numberOfVariables;
            int position3 = position2 + numberOfVariables;
            int position4 = position3 + numberOfVariables;
            // testvalues will be multiplied
            int multiplier = 1000000;

            EuclideanIntegerPoint[] breakingPoints = new EuclideanIntegerPoint[numberOfVariables];
            // define the space which will break the cluster algorithm
            for (int i = 0; i < numberOfVariables; i++) {
                int points[] = { position1, position2, position3, position4 };
                // multiply the values
                for (int j = 0; j < points.length; j++) {
                    points[j] = points[j] * multiplier;
                }
                EuclideanIntegerPoint euclideanIntegerPoint = new EuclideanIntegerPoint(points);
                breakingPoints[i] = euclideanIntegerPoint;
                position1 = position1 + numberOfVariables;
                position2 = position2 + numberOfVariables;
                position3 = position3 + numberOfVariables;
                position4 = position4 + numberOfVariables;
            }

            for (int n = 2; n < 27; ++n) {
                List<Cluster<EuclideanIntegerPoint>> clusters =
                    transformer.cluster(Arrays.asList(breakingPoints), n, 100);
                Assert.assertEquals(n, clusters.size());
                int sum = 0;
                for (Cluster<EuclideanIntegerPoint> cluster : clusters) {
                    sum += cluster.getPoints().size();
                }
                Assert.assertEquals(numberOfVariables, sum);
            }
        }

    }

    /**
     * A helper class for testSmallDistances(). This class is similar to EuclideanIntegerPoint, but
     * it defines a different distanceFrom() method that tends to return distances less than 1.
     */
    private class CloseIntegerPoint implements Clusterable<CloseIntegerPoint> {
        public CloseIntegerPoint(EuclideanIntegerPoint point) {
            euclideanPoint = point;
        }

        public double distanceFrom(CloseIntegerPoint p) {
            return euclideanPoint.distanceFrom(p.euclideanPoint) * 0.001;
        }

        public CloseIntegerPoint centroidOf(Collection<CloseIntegerPoint> p) {
            Collection<EuclideanIntegerPoint> euclideanPoints =
                new ArrayList<EuclideanIntegerPoint>();
            for (CloseIntegerPoint point : p) {
                euclideanPoints.add(point.euclideanPoint);
            }
            return new CloseIntegerPoint(euclideanPoint.centroidOf(euclideanPoints));
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CloseIntegerPoint)) {
                return false;
            }
            CloseIntegerPoint p = (CloseIntegerPoint) o;

            //System.out.println("euclideanPoint = " + euclideanPoint);
            //System.out.println("p.euclideanPoint = " + p.euclideanPoint);
            boolean ret = euclideanPoint.equals(p.euclideanPoint);
            //System.out.println("ret = " + ret);
            return ret;
        }

        @Override
        public int hashCode() {
            return euclideanPoint.hashCode();
        }

        private EuclideanIntegerPoint euclideanPoint;
    }

    /**
     * Test points that are very close together. See issue MATH-546.
     */
    @Fuzz
    public void testSmallDistances(
                                    @InRange(minInt=1, maxInt=10) int p1,
                                    @InRange(minInt=1, maxInt=10) int r1
                                  ) {
        try {
            // Create a bunch of CloseIntegerPoints. Most are identical, but one is different by a
            // small distance.
            int[] uniqueArray = {p1};
            CloseIntegerPoint uniquePoint =
                    new CloseIntegerPoint(new EuclideanIntegerPoint(uniqueArray));

            Collection<CloseIntegerPoint> points = new ArrayList<CloseIntegerPoint>();
            final int NUM_REPEATED_POINTS = 10 * 1000;
            for (int i = 0; i < NUM_REPEATED_POINTS; ++i) {
                points.add(new CloseIntegerPoint(new EuclideanIntegerPoint(new int[]{r1*i})));
            }
            points.add(uniquePoint);

            // Ask a KMeansPlusPlusClusterer to run zero iterations (i.e., to simply choose initial
            // cluster centers).
            final long RANDOM_SEED = 0;
            final int NUM_CLUSTERS = 2;
            final int NUM_ITERATIONS = 0;
            KMeansPlusPlusClusterer<CloseIntegerPoint> clusterer =
                    new KMeansPlusPlusClusterer<CloseIntegerPoint>(new Random(RANDOM_SEED));
            List<Cluster<CloseIntegerPoint>> clusters =
                    clusterer.cluster(points, NUM_CLUSTERS, NUM_ITERATIONS);

            // Check that one of the chosen centers is the unique point.
            XStream stream = new XStream();
            boolean uniquePointIsCenter = false;
            if (Log.runBuggyVersion) Log.logOutIf(true, () -> new String[]{ "No exception" });
            else Log.ignoreOut();
        } catch (Exception e){
            Log.logOutIf(true, () -> new String[] {e.getClass().toString()});
        }
    }
}
