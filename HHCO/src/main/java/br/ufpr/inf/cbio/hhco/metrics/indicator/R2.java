package br.ufpr.inf.cbio.hhco.metrics.indicator;

import br.ufpr.inf.cbio.hhco.metrics.utilityfunction.UtilityFunction;
import org.uma.jmetal.qualityindicator.QualityIndicator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.naming.impl.SimpleDescribedEntity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.StringTokenizer;

/**
 * TODO: Add comments here
 *
 * @param <Evaluate>
 */
@SuppressWarnings("serial")
public class R2<Evaluate extends List<? extends Solution<?>>>
        extends SimpleDescribedEntity
        implements QualityIndicator<Evaluate, Double> {

    private final double[][] lambda;

    private final Front referenceParetoFront;
    private UtilityFunction function;

    /**
     * Creates a new instance of the R2 indicator for a problem with two
     * objectives and 100 lambda vectors
     *
     * @param referenceParetoFront
     */
    public R2(Front referenceParetoFront) {
        // by default it creates an R2 indicator for a two dimensions problem and
        // uses only 100 weight vectors for the R2 computation
        this(100, referenceParetoFront);
    }

    /**
     * Creates a new instance of the R2 indicator for a problem with two
     * objectives and 100 lambda vectors
     */
    public R2() {
        // by default it creates an R2 indicator for a two dimensions problem and
        // uses only 100 weight vectors for the R2 computation
        this(100);
    }

    /**
     * Creates a new instance of the R2 indicator for a problem with two
     * objectives and N lambda vectors
     *
     * @param nVectors
     */
    public R2(int nVectors) {
        this(nVectors, null);
    }

    /**
     * Constructor Creates a new instance of the R2 indicator for nDimensiosn It
     * loads the weight vectors from the file fileName
     *
     * @param file
     * @param referenceParetoFront
     * @throws java.io.IOException
     */
    public R2(String file, Front referenceParetoFront) throws java.io.IOException {
        this(readWeightsFrom(file), referenceParetoFront);
    }

    /**
     * Creates a new instance of the R2 indicator for a problem with two
     * objectives and N lambda vectors
     *
     * @param nVectors
     * @param referenceParetoFront
     */
    public R2(int nVectors, Front referenceParetoFront) {
        // by default it creates an R2 indicator for a two dimensions problem and
        // uses only <code>nVectors</code> weight vectors for the R2 computation
        this(generateWeights(nVectors), referenceParetoFront);
    }

    public R2(double[][] lambda, Front referenceParetoFront) {
        // by default it creates an R2 indicator for a two dimensions problem and
        // uses only <code>nVectors</code> weight vectors for the R2 computation
        super("R2", "R2 quality indicator");
        this.lambda = lambda;
        this.referenceParetoFront = referenceParetoFront;
    }

    public R2(double[][] lambda, Front referenceParetoFront, UtilityFunction function) {
        this(lambda, referenceParetoFront);
        this.function = function;
    }

    private static double[][] generateWeights(int nVectors) {
        double[][] lambda = new double[nVectors][2];
        for (int n = 0; n < nVectors; n++) {
            double a = 1.0 * n / (nVectors - 1);
            lambda[n][0] = a;
            lambda[n][1] = 1 - a;
        }
        return lambda;
    }

    private static double[][] readWeightsFrom(String file) throws java.io.IOException {
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        String line = br.readLine();
        double[][] lambda;
        if (line == null) {
            lambda = null;
        } else {
            int numberOfObjectives = (new StringTokenizer(line)).countTokens();
            int numberOfVectors = (int) br.lines().count();

            lambda = new double[numberOfVectors][numberOfObjectives];

            int index = 0;
            while (line != null) {
                StringTokenizer st = new StringTokenizer(line);
                for (int i = 0; i < numberOfObjectives; i++) {
                    lambda[index][i] = new Double(st.nextToken());
                }
                index++;
                line = br.readLine();
            }

            br.close();
        }
        return lambda;
    }

    /**
     * Constructor Creates a new instance of the R2 indicator for nDimensiosn It
     * loads the weight vectors from the file fileName
     *
     * @param file
     * @throws java.io.IOException
     */
    public R2(String file) throws java.io.IOException {
        this(file, null);
    } // R2

    @Override
    public Double evaluate(Evaluate solutionList) {
        return r2(new ArrayFront(solutionList));
    }

    @Override
    public String getName() {
        return super.getName();
    }

    public double r2(Front front) {
        double[] minimumValues;
        double[] maximumValues;

        if (this.referenceParetoFront != null) {
            // STEP 1. Obtain the maximum and minimum values of the Pareto front
            maximumValues = FrontUtils.getMaximumValues(this.referenceParetoFront);
            minimumValues = FrontUtils.getMinimumValues(this.referenceParetoFront);

            front = getNormalizedFront(front, maximumValues, minimumValues);
        }

        int numberOfObjectives = front.getPoint(0).getDimension();

        // STEP 3. compute all the matrix of Tschebyscheff values if it is null
        double[][] matrix = new double[front.getNumberOfPoints()][lambda.length];
        for (int i = 0; i < front.getNumberOfPoints(); i++) {
            for (int j = 0; j < lambda.length; j++) {
                matrix[i][j] = function.execute(lambda[j], front.getPoint(i), numberOfObjectives);
            }
        }

        double sum = 0.0;
        for (int i = 0; i < lambda.length; i++) {
            double tmp = matrix[0][i];
            for (int j = 1; j < front.getNumberOfPoints(); j++) {
                tmp = Math.min(tmp, matrix[j][i]);
            }
            sum += tmp;
        }
        return sum / (double) lambda.length;
    }

    private Front getNormalizedFront(Front front, double[] maximumValues, double[] minimumValues) {

        Front normalizedFront = new ArrayFront(front);
        int numberOfPointDimensions = front.getPoint(0).getDimension();

        for (int i = 0; i < front.getNumberOfPoints(); i++) {
            for (int j = 0; j < numberOfPointDimensions; j++) {
                if ((maximumValues[j] - minimumValues[j]) == 0) {
                    normalizedFront.getPoint(i).setValue(j, 0.0);
                } else {

                    normalizedFront.getPoint(i).setValue(j, (front.getPoint(i).getValue(j)
                            - minimumValues[j]) / (maximumValues[j] - minimumValues[j]));
                }
            }
        }
        return normalizedFront;
    }
}
