/* (c) Copyright by Man YUAN */
package net.epsilony.tb.solid;

import net.epsilony.tb.IntIdentityMap;

/**
 *
 * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
 */
public class RawSegment implements Segment {

    protected int diffOrder = 0;
    protected Node start;
    public int id = IntIdentityMap.NULL_INDEX_SUPREMUM;
    protected Segment pred;
    protected Segment succ;
    Ring parent;

    @Override
    public int getDiffOrder() {
        return diffOrder;
    }

    @Override
    public Node getStart() {
        return start;
    }

    public double[] getStartCoord() {
        return start.coord;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Segment getPred() {
        return pred;
    }

    @Override
    public Node getEnd() {
        return succ.getStart();
    }

    public double[] getEndCoord() {
        return getEnd().coord;
    }

    @Override
    public Segment getSucc() {
        return succ;
    }

    @Override
    public void setDiffOrder(int diffOrder) {
        if (diffOrder < 0 || diffOrder > 1) {
            throw new UnsupportedOperationException("Only support 0 and 1, not :" + diffOrder);
        }
        this.diffOrder = diffOrder;
    }

    @Override
    public void setStart(Node start) {
        this.start = start;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void setPred(Segment pred) {
        this.pred = pred;
    }

    @Override
    public void setSucc(Segment succ) {
        this.succ = succ;
    }

    @Override
    public double[] values(double x, double[] results) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void bisect() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Ring getParent() {
        return parent;
    }

    public void setParent(Ring parent) {
        this.parent = parent;
    }

}
