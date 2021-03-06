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
package org.apache.commons.math3.geometry.euclidean.twod;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.oned.Euclidean1D;
import org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.math3.geometry.partitioning.RegionFactory;
import org.junit.Assert;
import org.junit.Test;

//Additional packages
import static org.junit.Assert.*;
import com.pholser.junit.quickcheck.*;
import com.pholser.junit.quickcheck.generator.*;
import com.pholser.junit.quickcheck.random.*;
import edu.berkeley.cs.jqf.fuzz.*;
import org.junit.runner.RunWith;
//import edu.berkeley.cs.jqf.fuzz.reach.Log;
import anonymous.log.Log;


@RunWith(JQF.class)
public class JQF_SubLineTest {
    @Test
    public void testEndPoints() {
        Vector2D p1 = new Vector2D(-1, -7);
        Vector2D p2 = new Vector2D(7, -1);
        Segment segment = new Segment(p1, p2, new Line(p1, p2));
        SubLine sub = new SubLine(segment);
        List<Segment> segments = sub.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertEquals(0.0, new Vector2D(-1, -7).distance(segments.get(0).getStart()), 1.0e-10);
        Assert.assertEquals(0.0, new Vector2D( 7, -1).distance(segments.get(0).getEnd()), 1.0e-10);
    }

    @Test
    public void testNoEndPoints() {
        SubLine wholeLine = new Line(new Vector2D(-1, 7), new Vector2D(7, 1)).wholeHyperplane();
        List<Segment> segments = wholeLine.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getX()) &&
                          segments.get(0).getStart().getX() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getY()) &&
                          segments.get(0).getStart().getY() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getX()) &&
                          segments.get(0).getEnd().getX() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getY()) &&
                          segments.get(0).getEnd().getY() < 0);
    }

    @Test
    public void testNoSegments() {
        SubLine empty = new SubLine(new Line(new Vector2D(-1, -7), new Vector2D(7, -1)),
                                    new RegionFactory<Euclidean1D>().getComplement(new IntervalsSet()));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testSeveralSegments() {
        SubLine twoSubs = new SubLine(new Line(new Vector2D(-1, -7), new Vector2D(7, -1)),
                                    new RegionFactory<Euclidean1D>().union(new IntervalsSet(1, 2),
                                                                           new IntervalsSet(3, 4)));
        List<Segment> segments = twoSubs.getSegments();
        Assert.assertEquals(2, segments.size());
    }

    @Test
    public void testHalfInfiniteNeg() {
        SubLine empty = new SubLine(new Line(new Vector2D(-1, -7), new Vector2D(7, -1)),
                                    new IntervalsSet(Double.NEGATIVE_INFINITY, 0.0));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getX()) &&
                          segments.get(0).getStart().getX() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getY()) &&
                          segments.get(0).getStart().getY() < 0);
        Assert.assertEquals(0.0, new Vector2D(3, -4).distance(segments.get(0).getEnd()), 1.0e-10);
    }

    @Test
    public void testHalfInfinitePos() {
        SubLine empty = new SubLine(new Line(new Vector2D(-1, -7), new Vector2D(7, -1)),
                                    new IntervalsSet(0.0, Double.POSITIVE_INFINITY));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertEquals(0.0, new Vector2D(3, -4).distance(segments.get(0).getStart()), 1.0e-10);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getX()) &&
                          segments.get(0).getEnd().getX() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getY()) &&
                          segments.get(0).getEnd().getY() > 0);
    }

    @Test
    public void testIntersectionInsideInside() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(3, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 2));
        Assert.assertEquals(0.0, new Vector2D(2, 1).distance(sub1.intersection(sub2, true)),  1.0e-12);
        Assert.assertEquals(0.0, new Vector2D(2, 1).distance(sub1.intersection(sub2, false)), 1.0e-12);
    }

    @Test
    public void testIntersectionInsideBoundary() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(3, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 1));
        Assert.assertEquals(0.0, new Vector2D(2, 1).distance(sub1.intersection(sub2, true)),  1.0e-12);
        Assert.assertNull(sub1.intersection(sub2, false));
    }

    @Test
    public void testIntersectionInsideOutside() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(3, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

    @Test
    public void testIntersectionBoundaryBoundary() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(2, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 1));
        Assert.assertEquals(0.0, new Vector2D(2, 1).distance(sub1.intersection(sub2, true)),  1.0e-12);
        Assert.assertNull(sub1.intersection(sub2, false));
    }

    @Test
    public void testIntersectionBoundaryOutside() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(2, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

    @Test
    public void testIntersectionOutsideOutside() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(1.5, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

    @Fuzz
    public void testIntersectionParallel(@InRange(minDouble=-5, maxDouble=5) double x) {
        final SubLine sub1 = new SubLine(new Vector2D(0, 0), new Vector2D(0, 3));
        final SubLine sub2 = new SubLine(new Vector2D(x, 0), new Vector2D(1, 3));
        if(x==0){
            Log.logOutIf(false,() -> new Boolean[]{sub1.intersection(sub2, true) == null, sub1.intersection(sub2, false) == null},new Boolean[]{false,true});
        }else if(x<0){
            Log.logOutIf(false,() -> new Boolean[]{sub1.intersection(sub2, true) == null, sub1.intersection(sub2, false) == null}, new Boolean[]{false,false});
        }else{
            Log.logOutIf(false,() -> new Boolean[]{sub1.intersection(sub2, true) == null, sub1.intersection(sub2, false) == null}, new Boolean[]{true,true});
        }
    }

}
