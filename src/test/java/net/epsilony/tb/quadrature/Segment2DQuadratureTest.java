/* (c) Copyright by Man YUAN */
package net.epsilony.tb.quadrature;

import net.epsilony.tb.RudeFactory;
import net.epsilony.tb.solid.ArcSegment2D;
import net.epsilony.tb.solid.Node;
import net.epsilony.tb.solid.Line;
import net.epsilony.tb.analysis.ArrvarFunction;
import net.epsilony.tb.analysis.Math2D;
import net.epsilony.tb.solid.Segment2DUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
 */
public class Segment2DQuadratureTest {

    public Segment2DQuadratureTest() {
    }

    @Test
    public void testLength() {
        final double val = 1.3;
        ArrvarFunction func = new ArrvarFunction() {
            @Override
            public double value(double[] vec) {
                return val;
            }
        };

        Line seg = new Line(new Node(1, -1));
        seg.setSucc(new Line(new Node(-2, 3)));
        double exp = 5 * val;
        boolean getHere = false;
        for (int deg = 1; deg < GaussLegendre.MAXPOINTS * 2 - 1; deg++) {
            Segment2DQuadrature sq = new Segment2DQuadrature();
            sq.setDegree(deg);
            sq.setSegment(seg);
            double act = sq.quadrate(func);
            assertEquals(exp, act, 1e-12);
            getHere = true;
        }
        assertTrue(getHere);
    }

    @Test
    public void testLadderX() {
        ArrvarFunction func = new ArrvarFunction() {
            @Override
            public double value(double[] vec) {
                return vec[0];
            }
        };

        Line seg = new Line(new Node(1, 2));
        seg.setSucc(new Line(new Node(-2, 6)));
        double exp = -2.5;
        boolean getHere = false;
        for (int deg = 1; deg < GaussLegendre.MAXPOINTS * 2 - 1; deg++) {
            Segment2DQuadrature sq = new Segment2DQuadrature();
            sq.setDegree(deg);
            sq.setSegment(seg);
            double act = sq.quadrate(func);
            assertEquals(exp, act, 1e-12);
            getHere = true;
        }
        assertTrue(getHere);

        seg.fractionize(7, new RudeFactory<>(Node.class));
        Segment2DQuadrature sq = new Segment2DQuadrature();
        sq.setSegment(seg);
        sq.setDegree(3);
        sq.setStartEndParameter(0, 7);
        double act = sq.quadrate(func);

        assertEquals(exp, act, 1e-14);

        getHere = false;
        for (Segment2DQuadraturePoint qp : sq) {
            getHere = true;
            qp.segment.setDiffOrder(0);
            double[] coord = qp.segment.values(qp.segmentParameter, null);
            assertArrayEquals(coord, qp.coord, 1e-14);
        }
        assertTrue(getHere);
    }

    @Test
    public void testLadderY() {
        ArrvarFunction func = new ArrvarFunction() {
            @Override
            public double value(double[] vec) {
                return vec[1];
            }
        };

        Line seg = new Line(new Node(1, 2));
        seg.setSucc(new Line(new Node(-2, 6)));
        double exp = 20;
        boolean getHere = false;
        for (int deg = 1; deg < GaussLegendre.MAXPOINTS * 2 - 1; deg++) {
            Segment2DQuadrature sq = new Segment2DQuadrature();
            sq.setDegree(deg);
            sq.setSegment(seg);
            double act = sq.quadrate(func);
            assertEquals(exp, act, 1e-12);
            getHere = true;
        }
        assertTrue(getHere);
    }

    @Test
    public void testArcLength() {
        double startAngle = Math.PI * 0.33;
        double endAngle = Math.PI * 0.55;
        double xTrans = 13.1;
        double yTrans = -7.2;
        double radius = 33;
        double exp = Math.PI * (0.55 - 0.33) * radius;
        ArcSegment2D arc = new ArcSegment2D();
        arc.setStart(new Node(xTrans + radius * Math.cos(startAngle), yTrans + radius * Math.sin(startAngle)));
        arc.setRadius(radius);
        arc.setSucc(new Line(
                new Node(
                xTrans + radius * Math.cos(endAngle),
                yTrans + radius * Math.sin(endAngle))));
        Segment2DQuadrature sq = new Segment2DQuadrature();
        sq.setDegree(1);
        boolean beenHere = false;
        for (int deg = 3; deg < GaussLegendre.MAXPOINTS * 2 - 1; deg++) {
            sq.setDegree(deg);
            sq.setSegment(arc);
            double act = sq.quadrate(new ArrvarFunction() {
                @Override
                public double value(double[] vec) {
                    return 1;
                }
            });
            assertEquals(exp, act, 1e-14);
            beenHere = true;
        }
        assertTrue(beenHere);
    }

    @Test
    public void testFractionizedLine() {
        Line line = new Line(new Node(new double[]{-1, 1}));
        Line succ = new Line(new Node(new double[]{3, 4}));
        Segment2DUtils.link(line, succ);
        line.fractionize(7, new RudeFactory<>(Node.class));
        Segment2DQuadrature segment2DQuadrature = new Segment2DQuadrature();
        segment2DQuadrature.setDegree(2);
        segment2DQuadrature.setSegment(line);
        segment2DQuadrature.setStartEndParameter(0.1, 6.3);

        double expLen = 5 * 6.2 / 7;

        double actLen = 0;
        for (Segment2DQuadraturePoint qp : segment2DQuadrature) {
            actLen += qp.weight;
        }

        assertEquals(expLen, actLen, 1e-14);
    }
}
