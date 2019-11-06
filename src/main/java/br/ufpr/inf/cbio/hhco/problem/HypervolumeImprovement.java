/*
 * Copyright (C) 2019 Gian Fritsche <gmfritsche at inf.ufpr.br>
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
package br.ufpr.inf.cbio.hhco.problem;

import br.ufpr.inf.cbio.hhco.util.output.OutputWriter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.point.Point;
import org.uma.jmetal.util.point.impl.ArrayPoint;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 */
public class HypervolumeImprovement extends AbstractDoubleProblem {

    private static int DEFAULT_SAMPLESIZE = 1000000;
    private final double offset = 1.1;

    protected AbstractDoubleProblem problem;
    private final int numberOfObjectives;
    private Point referencePoint = null;
    private int sampleSize;
    private final List<Point> samples;
    protected final OutputWriter ow;

    public HypervolumeImprovement(AbstractDoubleProblem problem, OutputWriter ow) {
        this(problem, ow, DEFAULT_SAMPLESIZE);
    }

    public HypervolumeImprovement(AbstractDoubleProblem problem, OutputWriter ow, int sampleSize) {
        this.sampleSize = sampleSize;
        this.problem = problem;
        numberOfObjectives = problem.getNumberOfObjectives();
        try {
            String resource = "/referenceFronts/" + problem.getName() + "_" + problem.getNumberOfObjectives() + ".ref";
            Front front = new ArrayFront(resource);
            updateReferencePoint(front);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HypervolumeImprovement.class.getName()).log(Level.SEVERE, "Reference front not found for problem " + problem.getName() + " m=" + problem.getNumberOfObjectives() + ".", ex);
        }
        samples = generateSamples(sampleSize);
        this.ow = ow;
    }

    private List generateSamples(int sampleSize) {
        List<Point> s = new ArrayList<>(sampleSize);
        for (int i = 0; i < sampleSize; i++) {
            Point p = new ArrayPoint(numberOfObjectives);
            for (int j = 0; j < numberOfObjectives; j++) {
                p.setValue(j, JMetalRandom.getInstance().nextDouble(0.0, 1.0));
            }
            s.add(p);
        }
        return s;
    }

    @Override
    public void evaluate(DoubleSolution solution) {
        // compute objectives
        problem.evaluate(solution);
        double hv = updateHV(solution);
        ow.writeLine(Double.toString(hv));
    }

  public double updateHV(DoubleSolution solution) {
        // normalize
        Point point = new ArrayPoint(numberOfObjectives);
        for (int i = 0; i < numberOfObjectives; i++) {
            point.setValue(i, solution.getObjective(i) / (offset * referencePoint.getValue(i)));
        }
        // remove samples dominated by the solution
        Iterator<Point> i = samples.iterator();
        while (i.hasNext()) {
            Point p = i.next();
            if (solutionDominatesSample(point, p)) {
                i.remove();
            }
        }
        return (sampleSize - samples.size()) / (double) sampleSize;
    }

    public final void updateReferencePoint(Front front) {
        double[] maxObjectives = new double[numberOfObjectives];
        for (int i = 0; i < numberOfObjectives; i++) {
            maxObjectives[i] = 0;
        }

        for (int i = 0; i < front.getNumberOfPoints(); i++) {
            for (int j = 0; j < numberOfObjectives; j++) {
                if (maxObjectives[j] < front.getPoint(i).getValue(j)) {
                    maxObjectives[j] = front.getPoint(i).getValue(j);
                }
            }
        }

        if (referencePoint == null) {
            referencePoint = new ArrayPoint(numberOfObjectives);
            for (int i = 0; i < numberOfObjectives; i++) {
                referencePoint.setValue(i, Double.MAX_VALUE);
            }
        }

        for (int i = 0; i < referencePoint.getDimension(); i++) {
            referencePoint.setValue(i, maxObjectives[i]);
        }
    }

    @Override
    public DoubleSolution createSolution() {
        return problem.createSolution();
    }

    @Override
    public Double getLowerBound(int index) {
        return problem.getLowerBound(index);
    }

    @Override
    public Double getUpperBound(int index) {
        return problem.getUpperBound(index); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getNumberOfConstraints() {
        return problem.getNumberOfConstraints();
    }

    @Override
    public int getNumberOfVariables() {
        return problem.getNumberOfVariables();
    }

    @Override
    public int getNumberOfObjectives() {
        return problem.getNumberOfObjectives(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        return problem.getName(); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean solutionDominatesSample(Point solution, Point sample) {
        for (int d = 0; d < numberOfObjectives; d++) {
            if (sample.getValue(d) < solution.getValue(d)) {
                return false;
            }
        }
        return true;
    }

}
