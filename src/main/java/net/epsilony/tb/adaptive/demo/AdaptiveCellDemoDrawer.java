/*
 * Copyright (C) 2013 Man YUAN <epsilon@epsilony.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.epsilony.tb.adaptive.demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import net.epsilony.tb.adaptive.AdaptiveCell;
import net.epsilony.tb.adaptive.AdaptiveCellEdge;
import net.epsilony.tb.adaptive.AdaptiveUtils;
import net.epsilony.tb.solid.ui.NodeDrawer;
import net.epsilony.tb.analysis.Math2D;
import net.epsilony.tb.ui.SingleModelShapeDrawer;
import net.epsilony.tb.ui.UIUtils;

/**
 * 
 * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
 */
public class AdaptiveCellDemoDrawer extends SingleModelShapeDrawer {

    public static double DEFAULT_OPPOSITE_MARK_LENGTH = 15;
    public static Color DEFAULT_OPPOSITE_MARK_COLOR = Color.RED;
    AdaptiveCell cell;
    private NodeDrawer nodeDrawer = new NodeDrawer(null);
    private double oppositeMarkLength = DEFAULT_OPPOSITE_MARK_LENGTH;
    private Color oppositeMarkColor = DEFAULT_OPPOSITE_MARK_COLOR;
    public static boolean showOppositeMarks = true;

    public void setCell(AdaptiveCell cell) {
        this.cell = cell;
        Path2D path = new Path2D.Double();
        Iterator<AdaptiveCellEdge> edgeIter = cell.iterator();
        if (!edgeIter.hasNext()) {
            return;
        }
        AdaptiveCellEdge edge = edgeIter.next();
        path.moveTo(edge.getStart().getCoord()[0], edge.getStart().getCoord()[1]);
        while (edgeIter.hasNext()) {
            edge = edgeIter.next();
            path.lineTo(edge.getStart().getCoord()[0], edge.getStart().getCoord()[1]);
        }
        path.closePath();

        setShape(path);
    }

    protected final void _setQuadrangleAdaptiveCell(AdaptiveCell cell) {
    }

    public Color getNodeColor() {
        return nodeDrawer.getColor();
    }

    public void setNodeColor(Color color) {
        nodeDrawer.setColor(color);
    }

    @Override
    public void drawModel(Graphics2D g2) {
        super.drawModel(g2);

        for (AdaptiveCellEdge edge : cell) {
            nodeDrawer.setNode(edge.getStart());
            nodeDrawer.drawModel(g2);
            if (showOppositeMarks) {
                drawEdgeOpposite(g2, edge);
            }
        }
    }

    private void drawEdgeOpposite(Graphics2D g2, AdaptiveCellEdge edge) {

        if (edge.getOpposite() == null) {
            return;
        }
        double[] startCoord = edge.getStart().getCoord();
        double[] endCoord = edge.getEnd().getCoord();
        double[] midPoint = Math2D.pointOnSegment(startCoord, endCoord, 0.25, null);
        modelToComponentTransform.transform(midPoint, 0, midPoint, 0, 1);
        double[] edgeVec = Math2D.subs(endCoord, startCoord, null);
        double[] markVec = new double[] { -edgeVec[1], edgeVec[0] };
        UIUtils.transformVector(modelToComponentTransform, markVec, markVec);
        Math2D.normalize(markVec, markVec);
        Math2D.scale(markVec, oppositeMarkLength, markVec);
        Path2D path = new Path2D.Double();

        g2.setColor(oppositeMarkColor);

        AdaptiveCellEdge opp = (AdaptiveCellEdge) edge.getOpposite();
        double[] oppositeMid = Math2D.pointOnSegment(opp.getStart().getCoord(), opp.getEnd().getCoord(), 0.25, null);
        modelToComponentTransform.transform(oppositeMid, 0, oppositeMid, 0, 1);
        path.moveTo(midPoint[0] + markVec[0], midPoint[1] + markVec[1]);
        path.lineTo(oppositeMid[0], oppositeMid[1]);

        g2.draw(path);
    }

    @Override
    public void setComponent(Component component) {
        super.setComponent(component);
        nodeDrawer.setComponent(component);
    }

    @Override
    public void setModelToComponentTransform(AffineTransform modelToComponentTransform) {
        super.setModelToComponentTransform(modelToComponentTransform);
        nodeDrawer.setModelToComponentTransform(modelToComponentTransform);
    }

    public AdaptiveCell getCell() {
        return cell;
    }

    public boolean isComponentPointInside(int x, int y) throws NoninvertibleTransformException {
        if (cell == null) {
            return false;
        }
        Point2D pt;
        pt = modelToComponentTransform.inverseTransform(new Point2D.Double(x, y), null);
        return AdaptiveUtils.isPointRestrictlyInsideCell(cell, pt.getX(), pt.getY());
    }
}
