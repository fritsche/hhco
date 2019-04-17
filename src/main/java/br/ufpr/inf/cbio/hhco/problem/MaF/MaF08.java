package br.ufpr.inf.cbio.hhco.problem.MaF;

import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

/**
 * Class representing problem MaF08
 */
public class MaF08 extends AbstractDoubleProblem {

    private final String name;

    public static double const8[][];

    /**
     * Creates a MaF03 problem instance
     *
     * @param numberOfVariables Number of variables
     * @param numberOfObjectives Number of objective functions
     */
    public MaF08(Integer numberOfVariables,
            Integer numberOfObjectives) {
        setNumberOfVariables(2); // always 2
        setNumberOfObjectives(numberOfObjectives);
        setNumberOfConstraints(0);
        this.name = "MaF08";

        double r = 1;
        const8 = polygonpoints(numberOfObjectives, r);

        List<Double> lower = new ArrayList<>(getNumberOfVariables()), upper = new ArrayList<>(getNumberOfVariables());

        for (int var = 0; var < numberOfVariables; var++) {
            lower.add(-10000.0);
            upper.add(10000.0);
        } //for

        setLowerLimit(lower);
        setUpperLimit(upper);

    }

    /**
     * Evaluates a solution
     *
     * @param solution The solution to evaluate
     */
    @Override
    public void evaluate(DoubleSolution solution) {

        int numberOfVariables_ = solution.getNumberOfVariables();
        int numberOfObjectives_ = solution.getNumberOfObjectives();

        double[] x = new double[numberOfVariables_];
        double[] f = new double[numberOfObjectives_];

        for (int i = 0; i < numberOfVariables_; i++) {
            x[i] = solution.getVariableValue(i);
        }
//	evaluate f
        for (int i = 0; i < numberOfObjectives_; i++) {
            f[i] = Math.sqrt(Math.pow(const8[i][0] - x[0], 2) + Math.pow(const8[i][1] - x[1], 2));
        }

        for (int i = 0; i < numberOfObjectives_; i++) {
            solution.setObjective(i, f[i]);
        }

    }

    public static double[][] polygonpoints(int m, double r) {
        double[] startp = new double[2];
        startp[0] = 0;
        startp[1] = 1;
        double[][] p1 = new double[m][2];
        double[][] p = new double[m][2];
        p1[0] = startp;
//	vertexes with the number of edges(m),start vertex(startp),radius(r)
        for (int i = 1; i < m; i++) {
            p1[i] = nextPoint(2 * Math.PI / m * i, startp, r);
        }
        for (int i = 0; i < m; i++) {
            p[i] = p1[m - i - 1];
        }
        return p;
    }

    public static double[] nextPoint(double arc, double[] startp, double r) {// arc is radians��evaluation the next vertex with arc and r
        double[] p = new double[2];
        p[0] = startp[0] - r * Math.sin(arc);
        p[1] = r * Math.cos(arc);
        return p;
    }

    @Override
    public String getName() {
        return name;
    }
}
