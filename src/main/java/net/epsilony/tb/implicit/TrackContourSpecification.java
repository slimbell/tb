/* (c) Copyright by Man YUAN */
package net.epsilony.tb.implicit;

import java.util.Arrays;
import net.epsilony.tb.analysis.Math2D;

/**
 *
 * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
 */
public class TrackContourSpecification {

    public static final double DEFAULT_HEAD_CHECK_ANGLE = Math.PI / 3;
    public static final double MIN_MAX_SEGMENT_LENGTH_RATIO = 0.01;
    public static double DEFAULT_EXPECT_SEGMENT_LENGTH = 1;
    public static double DEFAULT_MAX_SEGMENT_LENGTH = 1.5;
    public static final double DEFAULT_MAX_CURVE = Math.PI / 3;
    public static final double DEFAULT_MAX_PERPENDICULAR_VIOLATION_ANGLE = Math.PI / 3;
    public static final double DEFAULT_EXPECT_CURVE = Math.PI / 12;
    public static final double DEFAULT_CLOSE_HEAD_SEARCH_ANGLE = Math.PI / 2;
    //
    private double headCheckAngle = DEFAULT_HEAD_CHECK_ANGLE;
    private double headCheckCos = Math.cos(Math.PI / 2 - headCheckAngle);
    private double expectSegmentLength = DEFAULT_EXPECT_SEGMENT_LENGTH;
    private double maxSegmentLength = DEFAULT_MAX_SEGMENT_LENGTH;
    private double minSegmentLength = maxSegmentLength * MIN_MAX_SEGMENT_LENGTH_RATIO;
    private double maxCurve = DEFAULT_MAX_CURVE;
    private double curveCosLowerLimit = Math.cos(maxCurve);
    private double maxPerpendicularViolationAngle = DEFAULT_MAX_PERPENDICULAR_VIOLATION_ANGLE;
    private double perpendicularViolationSinLowerLimit = Math.sin(Math.PI / 2 - maxPerpendicularViolationAngle);
    private double expectCurve = DEFAULT_EXPECT_CURVE;
    private double expectCurveParameter = Math.sqrt(1 - Math.cos(expectCurve));
    private double closeHeadSearchAngle = DEFAULT_CLOSE_HEAD_SEARCH_ANGLE;
    private double closeHeadSearchSin = Math.sin(Math.PI / 2 - DEFAULT_CLOSE_HEAD_SEARCH_ANGLE);
    //
    private static final double ROUGH_POINT_DISTANCE_SHRINK = 0.8;

    public double genNextRoughPointDistance(ContourNode ndA, ContourNode ndB) {
        double d = Math2D.distance(ndA.getCoord(), ndB.getCoord());
        double[] funcValueA = ndA.getFunctionValue();
        double[] funcValueB = ndB.getFunctionValue();
        double gax = funcValueA[1];
        double gay = funcValueA[2];
        double gbx = funcValueB[1];
        double gby = funcValueB[2];
        double gradCos = Math2D.cos(gax, gay, gbx, gby);

        double result = ROUGH_POINT_DISTANCE_SHRINK * d * expectCurveParameter / Math.sqrt(1 - gradCos);

        if (result > maxSegmentLength) {
            result = maxSegmentLength;
        }

        if (result < minSegmentLength) {
            result = minSegmentLength;
        }
        return result;
    }

    public boolean isOtherHeadInCandidateDirection(
            ContourNode otherHead,
            double[] nodeCoord,
            double[] nodeUnitNormal) {

        double[] headCoord = otherHead.getCoord();

        double[] vec = Math2D.subs(headCoord, nodeCoord, null);
        Math2D.normalize(vec, vec);
        double outer = Math2D.cross(nodeUnitNormal, vec);
        if (outer < closeHeadSearchSin) {
            return false;
        }

        double[] headNormal = Arrays.copyOfRange(otherHead.getFunctionValue(), 1, 3);
        Math2D.normalize(headNormal, headNormal);
        double inner = Math2D.dot(headNormal, nodeUnitNormal);
        if (inner <= 0) {
            return false;
        }

        return true;
    }

    public boolean isHeadPointNormalEligible(ContourNode head, double[] unitRoughNextDirection) {
        double[] funcV = head.getFunctionValue();
        double nx = funcV[1];
        double ny = funcV[2];
        double nNorm = Math.sqrt(nx * nx + ny * ny);
        nx /= nNorm;
        ny /= nNorm;
        double mx = unitRoughNextDirection[0];
        double my = unitRoughNextDirection[1];
        double outer = Math2D.cross(nx, ny, mx, my);
        double inner = Math2D.dot(nx, ny, mx, my);
        if (outer <= 0 || inner > headCheckCos) {
            return false;
        }
        return true;
    }

    public boolean isSegmentEligible(ContourNode start, ContourNode end) {

        double[] startFuncV = start.getFunctionValue();
        double[] endFuncV = end.getFunctionValue();
        double[] startNormal = Arrays.copyOfRange(startFuncV, 1, startFuncV.length);
        double[] endNormal = Arrays.copyOfRange(endFuncV, 1, endFuncV.length);
        Math2D.normalize(startNormal, startNormal);
        Math2D.normalize(endNormal, endNormal);

        double curveCos = Math2D.dot(startNormal, endNormal);
        if (curveCos < curveCosLowerLimit) {
            return false;
        }

        double[] startCoord = start.getCoord();
        double[] endCoord = end.getCoord();
        double[] vec = Math2D.subs(endCoord, startCoord, null);
        Math2D.normalize(vec, vec);

        double outer = Math2D.cross(startNormal, vec);
        if (outer < perpendicularViolationSinLowerLimit) {
            return false;
        }
        outer = Math2D.cross(endNormal, vec);
        if (outer < perpendicularViolationSinLowerLimit) {
            return false;
        }

        return true;
    }

    public double getCloseHeadSearchAngle() {
        return closeHeadSearchAngle;
    }

    public void setCloseHeadSearchAngle(double closeHeadSearchAngle) {
        if (closeHeadSearchAngle <= 0 || closeHeadSearchAngle >= Math.PI / 2) {
            throw new IllegalArgumentException("should be in (0,pi/2), not " + closeHeadSearchAngle);
        }
        closeHeadSearchSin = Math.sin(Math.PI / 2 - closeHeadSearchAngle);
        this.closeHeadSearchAngle = closeHeadSearchAngle;
    }

    public double getExpectCurve() {
        return expectCurve;
    }

    public void setExpectCurve(double expectCurve) {
        if (expectCurve <= 0 || expectCurve >= Math.PI / 2) {
            throw new IllegalArgumentException("expect curver should be in (0, pi/2), not " + expectCurve);
        }
        expectCurveParameter = Math.sqrt(1 - Math.cos(expectCurve));
        this.expectCurve = expectCurve;
    }

    public double getHeadCheckAngle() {
        return headCheckAngle;
    }

    public void setHeadCheckAngle(double headCheckAngle) {
        if (headCheckAngle < 0 || headCheckAngle > Math.PI / 2) {
            throw new IllegalArgumentException("angle should be in (0,pi/2), not " + headCheckAngle);
        }
        this.headCheckAngle = headCheckAngle;
        this.headCheckCos = Math.cos(Math.PI / 2 - headCheckCos);
    }

    public double getExpectSegmentLength() {
        return expectSegmentLength;
    }

    public void setExpectSegmentLength(double expectSegmentLength) {
        if (expectSegmentLength < 0) {
            throw new IllegalArgumentException();
        }
        this.expectSegmentLength = expectSegmentLength;
    }

    public double getMaxSegmentLength() {
        return maxSegmentLength;
    }

    public void setMaxSegmentLength(double maxSegmentLength) {
        if (maxSegmentLength < expectSegmentLength) {
            throw new IllegalArgumentException();
        }
        if (maxSegmentLength * MIN_MAX_SEGMENT_LENGTH_RATIO > expectSegmentLength) {
            throw new IllegalArgumentException();
        }
        this.maxSegmentLength = maxSegmentLength;
        minSegmentLength = maxSegmentLength * MIN_MAX_SEGMENT_LENGTH_RATIO;
    }

    public double getMaxCurve() {
        return maxCurve;
    }

    public void setMaxCurve(double maxGradIntersectionAngle) {
        if (maxGradIntersectionAngle > Math.PI / 2 || maxGradIntersectionAngle < 0) {
            throw new IllegalArgumentException("should be in [0,pi/2], not" + maxGradIntersectionAngle);
        }
        curveCosLowerLimit = Math.cos(maxCurve);
        this.maxCurve = maxGradIntersectionAngle;
    }

    public double getMaxPerpendicularViolationAngle() {
        return maxPerpendicularViolationAngle;
    }

    public void setMaxPerpendicularViolationAngle(double maxPerpendicularViolationAngle) {
        if (maxPerpendicularViolationAngle > Math.PI / 2 || maxPerpendicularViolationAngle < 0) {
            throw new IllegalArgumentException("should be in [0,pi/2], not" + maxPerpendicularViolationAngle);
        }
        perpendicularViolationSinLowerLimit = Math.sin(Math.PI / 2 - maxPerpendicularViolationAngle);
        this.maxPerpendicularViolationAngle = maxPerpendicularViolationAngle;
    }

    public double getHeadCheckCos() {
        return headCheckCos;
    }

    public double getMinSegmentLength() {
        return minSegmentLength;
    }

    public double getCurveCosLowerLimit() {
        return curveCosLowerLimit;
    }

    public double getPerpendicularViolationSinLowerLimit() {
        return perpendicularViolationSinLowerLimit;
    }

    public double getExpectCurveParameter() {
        return expectCurveParameter;
    }

    public double getCloseHeadSearchSin() {
        return closeHeadSearchSin;
    }
}