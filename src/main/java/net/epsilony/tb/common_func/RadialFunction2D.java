/* (c) Copyright by Man YUAN */
package net.epsilony.tb.common_func;

import gnu.trove.list.array.TDoubleArrayList;
import net.epsilony.tb.MiscellaneousUtils;
import net.epsilony.tb.analysis.WithDiffOrder;
import net.epsilony.tb.analysis.WithDiffOrderUtil;
import net.epsilony.tb.synchron.SynchronizedClonable;

/**
 *
 * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
 */
public class RadialFunction2D implements WithDiffOrder, SynchronizedClonable<RadialFunction2D> {

    RadialFunctionCore coreFunc;

    @Override
    public int getDiffOrder() {
        return coreFunc.getDiffOrder();
    }

    @Override
    public void setDiffOrder(int diffOrder) {
        coreFunc.setDiffOrder(diffOrder);
    }

    public TDoubleArrayList[] initOutput(int capacity, TDoubleArrayList[] outputs) {
        return WithDiffOrderUtil.initOutput(outputs, capacity, 2, getDiffOrder());
    }

    public RadialFunction2D(RadialFunctionCore coreFunc) {
        this.coreFunc = coreFunc;
    }

    public RadialFunction2D() {
        this.coreFunc = new TripleSpline();
    }

    public TDoubleArrayList[] values(
            TDoubleArrayList[] dists,
            TDoubleArrayList influenceRads,
            TDoubleArrayList[] outputs) {
        TDoubleArrayList[] results = initOutput(dists[0].size(), outputs);
        boolean isUniRad = true;
        double uniRad = influenceRads.getQuick(0);
        if (influenceRads.size() > 1) {
            isUniRad = false;
        }
        int numRows = WithDiffOrderUtil.outputLength2D(getDiffOrder());
        double[] coreVals = new double[getDiffOrder() + 1];
        for (int i = 0; i < dists[0].size(); i++) {
            double dst = dists[0].getQuick(i);
            double rad = uniRad;
            if (!isUniRad) {
                rad = influenceRads.getQuick(i);
            }
            coreVals = coreFunc.valuesByDistance(dst / rad, coreVals);
            results[0].add(coreVals[0]);
            for (int j = 1; j < numRows; j++) {
                results[j].add(coreVals[1] / rad * dists[j].getQuick(i));
            }
        }
        return results;
    }

    public double[] values(double[] dists, double influenceRad, double[] output) {
        int outputLength = WithDiffOrderUtil.outputLength2D(getDiffOrder());
        if (null == output) {
            output = new double[outputLength];
        }
        coreFunc.valuesByDistance(dists[0] / influenceRad, output);
        if (getDiffOrder() >= 1) {
            double d = output[1];
            for (int j = 1; j < outputLength; j++) {
                output[j] = d / influenceRad * dists[j];
            }
        }
        return output;
    }

    @Override
    public RadialFunction2D synchronizeClone() {
        return new RadialFunction2D(coreFunc.synchronizeClone());
    }

    @Override
    public String toString() {
        return MiscellaneousUtils.simpleToString(this) + '{' + "coreFunc=" + coreFunc + '}';
    }
}
