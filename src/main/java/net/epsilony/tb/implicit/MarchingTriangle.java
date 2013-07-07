/* (c) Copyright by Man YUAN */
package net.epsilony.tb.implicit;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.tb.solid.Line2D;
import net.epsilony.tb.solid.Segment2DUtils;

/**
 *
 * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
 */
public abstract class MarchingTriangle extends AbstractTriangleContourBuilder {

    protected List<TriangleContourCell> openRingsHeadsCells = new LinkedList<>();

    @Override
    public void genContour() {
        prepareToGenContour();
        while (true) {
            TriangleContourCell headCell = nextUnvisitedCellWithContour();
            if (null == headCell) {
                break;
            }
            genContourFromCell(headCell);
        }
    }

    @Override
    public void prepareToGenContour() {
        super.prepareToGenContour();
        openRingsHeadsCells.clear();
    }

    private void genContourFromCell(TriangleContourCell headCell) {
        headCell.setVisited(true);
        Line2D chainHead = new Line2D(genContourNode(headCell.getContourSourceEdge()));
        contourHeads.add(chainHead);

        openRingsHeadsCells.add(headCell);
        openRingsHeads.add(chainHead);
        TriangleContourCell contourCell = headCell;

        Line2D segment = chainHead;
        while (true) {
            TriangleContourCell nextContourCell = contourCell.nextContourCell();
            if (null == nextContourCell) {
                Line2D newSucc = new Line2D(genContourNode(contourCell.getContourDestinationEdge()));
                Segment2DUtils.link(segment, newSucc);
                break;
            } else {
                contourCell = nextContourCell;
            }

            if (contourCell == headCell) {
                Segment2DUtils.link(segment, chainHead);
                openRingsHeadsCells.remove(headCell);
                openRingsHeads.remove(chainHead);
                break;
            }

            if (contourCell.isVisited()) {
                boolean merged = tryMergeWithOpenRingHeads(contourCell, segment);
                if (merged) {
                    break;
                }
                throw new IllegalStateException();
            }

            contourCell.setVisited(true);
            setupFunctionData(contourCell);

            Line2D newSucc = new Line2D(genContourNode(contourCell.getContourSourceEdge()));
            Segment2DUtils.link(segment, newSucc);
            segment = newSucc;

        }
    }

    abstract protected ContourNode genContourNode(TriangleContourCellEdge contourSourceEdge);

    private boolean tryMergeWithOpenRingHeads(TriangleContourCell contourCell, Line2D segment) {
        Iterator<TriangleContourCell> openHeadCellIter = openRingsHeadsCells.iterator();
        Iterator<Line2D> openHeadSegIter = openRingsHeads.iterator();
        boolean findAndRemove = false;
        while (openHeadCellIter.hasNext()) {
            TriangleContourCell cell = openHeadCellIter.next();
            Line2D openRingHead = openHeadSegIter.next();
            if (cell == contourCell) {
                openHeadCellIter.remove();
                openHeadSegIter.remove();
                Segment2DUtils.link(segment, openRingHead);

                contourHeads.remove(openRingHead);

                findAndRemove = true;
                break;
            }
        }
        return findAndRemove;
    }

    public static class LinearInterpolate extends MarchingTriangle {

        @Override
        protected ContourNode genContourNode(TriangleContourCellEdge contourSourceEdge) {
            double[] resultCoord = genLinearInterpolateContourPoint(contourSourceEdge);
            ContourNode result = new ContourNode();
            result.setCoord(resultCoord);
            return result;
        }
    }

    public static class FreeGradient extends MarchingTriangle {

        ImplicitFunctionSolver solver = new SimpleGradientSolver();

        @Override
        protected ContourNode genContourNode(TriangleContourCellEdge contourSourceEdge) {
            double[] startPoint = genLinearInterpolateContourPoint(contourSourceEdge);
            if (solver.solve(startPoint)) {
                ContourNode result = new ContourNode();
                result.setCoord(solver.getSolution());
                result.setFunctionValue(solver.getFunctionValue());
                return result;
            } else {
                throw new IllegalStateException();
            }
        }

        public ImplicitFunctionSolver getSolver() {
            return solver;
        }

        public void setSolver(ImplicitFunctionSolver implicitFunctionSolver) {
            this.solver = implicitFunctionSolver;
            implicitFunctionSolver.setFunction(levelSetFunction);
        }
    }

    public static class OnEdge extends MarchingTriangle {

        BoundedImplicitFunctionSolver solver = new SimpleBisectionSolver();
        OnLineFunction onEdgeFunction = new OnLineFunction();

        public OnEdge() {
            solver.setFunction(onEdgeFunction);
            solver.setLowerBounds(new double[]{0});
            solver.setUpperBounds(new double[]{1});
            solver.setMaxEval(200);
            solver.setFunctionAbsoluteTolerence(1e-5);
        }

        public BoundedImplicitFunctionSolver getSolver() {
            return solver;
        }

        public void setSolver(BoundedImplicitFunctionSolver solver) {
            this.solver = solver;
            solver.setLowerBounds(new double[]{0});
            solver.setUpperBounds(new double[]{1});
            solver.setFunction(onEdgeFunction);
        }
        private final double[] solveStart = new double[]{0.5};

        @Override
        protected ContourNode genContourNode(TriangleContourCellEdge contourSourceEdge) {
            onEdgeFunction.prepareToSolve(contourSourceEdge.getStartCoord(), contourSourceEdge.getEndCoord());
            solveStart[0] = genLinearInterpolateParameter(contourSourceEdge);
            if (!solver.solve(solveStart)) {
                solver.solve(solveStart);
                throw new IllegalStateException();
            }
            ContourNode result = new ContourNode();
            result.setCoord(onEdgeFunction.coord);
            result.setFunctionValue(onEdgeFunction.levelSetFuncVal);
            return result;
        }
    }
}
