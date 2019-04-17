/*
 * Copyright (C) 2018 Gian Fritsche <gmfritsche@inf.ufpr.br>
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
package br.ufpr.inf.cbio.hhco.algorithm.ThetaDEA;

import br.ufpr.inf.cbio.hhco.util.Permutation;
import Jama.Matrix;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.SolutionListUtils;

/**
 * Based on the source distributed by Yuan Yuan
 * (https://github.com/yyxhdy/ManyEAs) <yyxhdy at gmail.com>
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 * @param <S>
 */
public class ThetaDEA<S extends Solution> implements Algorithm<List<S>> {

    protected int populationSize_;   // population size

    protected List<S> population_;   // current population
    List<S> offspringPopulation_;  // offspring population

    List<S> union_;    // the union of current population and offspring population

    int evaluations;   // evaluations

    protected SolutionListEvaluator<S> evaluator;

    double theta_;     // parameter theta 

    CrossoverOperator<S> crossover_; // crossover

    MutationOperator<S> mutation_;   // mutation operator

    boolean normalize_;  // normalization or not

    double[][] lambda_; // reference points

    double[] zideal_;   // ideal point

    double[] znadir_;   // nadir point

    double[][] extremePoints_; // extreme points

    protected Problem<S> problem_;

    protected int maxEvaluations;

    public ThetaDEA(ThetaDEABuilder<S> builder) {

        this.problem_ = builder.getProblem();
        /* set parameters */
        maxEvaluations = builder.getMaxEvaluations();

        theta_ = builder.getTheta();

        normalize_ = builder.getNormalize();

        populationSize_ = builder.getPopulationSize();

        crossover_ = builder.getCrossover(); // set the crossover operator
        mutation_ = builder.getMutation();  // set the mutation operator

    } // ThetaDEA 

    public void initExtremePoints() {
        int obj = problem_.getNumberOfObjectives();
        extremePoints_ = new double[obj][obj];
        for (int i = 0; i < obj; i++) {
            for (int j = 0; j < obj; j++) {
                extremePoints_[i][j] = 1.0e+30;
            }
        }

    }

    void getNextPopulation(List<S> pop) {
        ThetaRanking ranking = new ThetaRanking(pop, lambda_, zideal_,
                theta_, normalize_);

        int remain = populationSize_;
        int index = 0;
        List<S> front;
        population_.clear();

        // Obtain the next front
        front = ranking.getSubfront(index);

        while ((remain > 0) && (remain >= front.size())) {

            for (int k = 0; k < front.size(); k++) {
                population_.add(front.get(k));
            } // for

            // Decrement remain
            remain = remain - front.size();

            // Obtain the next front
            index++;
            if (remain > 0) {
                front = ranking.getSubfront(index);
            } // if
        } // while

        if (remain > 0) { // front contains individuals to insert

            int[] perm = new Permutation().intPermutation(front.size());
            for (int k = 0; k < remain; k++) {
                population_.add(front.get(perm[k]));
            } // for
        } // if
    }

    List<S>[] getParetoFronts() {

        List<S>[] sets = new List[2];
        Ranking ranking = (new DominanceRanking()).computeRanking(union_);

        int remain = populationSize_;
        int index = 0;
        List<S> front;
        List<S> mgPopulation = new ArrayList<>();

        front = ranking.getSubfront(index);

        sets[0] = front;

        while ((remain > 0) && (remain >= front.size())) {

            for (int k = 0; k < front.size(); k++) {
                mgPopulation.add(front.get(k));
            } // for

            // Decrement remain
            remain = remain - front.size();

            // Obtain the next front
            index++;
            if (remain > 0) {
                front = ranking.getSubfront(index);
            } // if
        }
        if (remain > 0) { // front contains individuals to insert
            for (int k = 0; k < front.size(); k++) {
                mgPopulation.add(front.get(k));
            }
        }

        sets[1] = mgPopulation;

        return sets;
    }

    void initPopulation() {

        population_ = new ArrayList<>(populationSize_);

        for (int i = 0; i < populationSize_; i++) {
            S newSolution = problem_.createSolution();
            problem_.evaluate(newSolution);
            population_.add(newSolution);
        }

    }

    void createOffSpringPopulation() {
        offspringPopulation_ = new ArrayList<>(populationSize_);

        for (int i = 0; i < populationSize_; i++) {
            doCrossover(i);
        }
    }

    void doCrossover(int i) {
        JMetalRandom generator = JMetalRandom.getInstance();
        int r;
        do {
            r = generator.nextInt(0, populationSize_ - 1);
        } while (r == i);

        List<S> parents = new ArrayList<>(2);

        parents.add(population_.get(i));
        parents.add(population_.get(r));

        List<S> offSpring = crossover_.execute(parents);

        mutation_.execute(offSpring.get(0));

        problem_.evaluate(offSpring.get(0));

        offspringPopulation_.add(offSpring.get(0));
    }

    void copyObjectiveValues(double[] array, S individual) {
        for (int i = 0; i < individual.getNumberOfObjectives(); i++) {
            array[i] = individual.getObjective(i);
        }
    }

    double asfFunction(S sol, int j) {
        double max = Double.MIN_VALUE;
        double epsilon = 1.0E-6;

        int obj = problem_.getNumberOfObjectives();

        for (int i = 0; i < obj; i++) {

            double val = Math.abs((sol.getObjective(i) - zideal_[i])
                    / (znadir_[i] - zideal_[i]));

            if (j != i) {
                val = val / epsilon;
            }

            if (val > max) {
                max = val;
            }
        }

        return max;
    }

    double asfFunction(double[] ref, int j) {
        double max = Double.MIN_VALUE;
        double epsilon = 1.0E-6;

        int obj = problem_.getNumberOfObjectives();

        for (int i = 0; i < obj; i++) {

            double val = Math.abs((ref[i] - zideal_[i])
                    / (znadir_[i] - zideal_[i]));

            if (j != i) {
                val = val / epsilon;
            }

            if (val > max) {
                max = val;
            }
        }

        return max;
    }

    void initIdealPoint() {
        int obj = problem_.getNumberOfObjectives();
        zideal_ = new double[obj];
        for (int j = 0; j < obj; j++) {
            zideal_[j] = Double.MAX_VALUE;

            for (int i = 0; i < population_.size(); i++) {
                if (population_.get(i).getObjective(j) < zideal_[j]) {
                    zideal_[j] = population_.get(i).getObjective(j);
                }
            }
        }
    }

    void updateIdealPoint(List<S> pop) {

        if (zideal_ == null) {
            initIdealPoint();
            return;
        }

        for (int j = 0; j < problem_.getNumberOfObjectives(); j++) {
            for (int i = 0; i < pop.size(); i++) {
                if (pop.get(i).getObjective(j) < zideal_[j]) {
                    zideal_[j] = pop.get(i).getObjective(j);
                }
            }
        }
    }

    void initNadirPoint() {
        int obj = problem_.getNumberOfObjectives();
        znadir_ = new double[obj];
        for (int j = 0; j < obj; j++) {
            znadir_[j] = Double.MIN_VALUE;

            for (int i = 0; i < population_.size(); i++) {
                if (population_.get(i).getObjective(j) > znadir_[j]) {
                    znadir_[j] = population_.get(i).getObjective(j);
                }
            }
        }
    }

    void updateNadirPoint(List<S> pop) {

        if (znadir_ == null) {
            initNadirPoint();
            initExtremePoints();
            return;
        }

        updateExtremePoints(pop);

        int obj = problem_.getNumberOfObjectives();
        double[][] temp = new double[obj][obj];

        for (int i = 0; i < obj; i++) {
            for (int j = 0; j < obj; j++) {
                double val = extremePoints_[i][j] - zideal_[j];
                temp[i][j] = val;
            }
        }

        Matrix EX = new Matrix(temp);

        boolean sucess = true;

        if (EX.rank() == EX.getRowDimension()) {
            double[] u = new double[obj];
            for (int j = 0; j < obj; j++) {
                u[j] = 1;
            }

            Matrix UM = new Matrix(u, obj);

            Matrix AL = EX.inverse().times(UM);

            for (int j = 0; j < obj; j++) {

                double aj = 1.0 / AL.get(j, 0) + zideal_[j];

                if ((aj > zideal_[j]) && (!Double.isInfinite(aj)) && (!Double.isNaN(aj))) {
                    znadir_[j] = aj;
                } else {
                    sucess = false;
                    break;
                }
            }
        } else {
            sucess = false;
        }

        if (!sucess) {
            double[] zmax = computeMaxPoint(pop);
            System.arraycopy(zmax, 0, znadir_, 0, obj);
        }
    }

    public void updateExtremePoints(List<S> pop) {
        for (int i = 0; i < pop.size(); i++) {
            updateExtremePoints(pop.get(i));
        }
    }

    public void updateExtremePoints(S individual) {
        int obj = problem_.getNumberOfObjectives();
        for (int i = 0; i < obj; i++) {
            double asf1 = asfFunction(individual, i);
            double asf2 = asfFunction(extremePoints_[i], i);

            if (asf1 < asf2) {
                copyObjectiveValues(extremePoints_[i], individual);
            }
        }
    }

    double[] computeMaxPoint(List<S> pop) {
        int obj = problem_.getNumberOfObjectives();
        double zmax[] = new double[obj];
        for (int j = 0; j < obj; j++) {
            zmax[j] = Double.MIN_VALUE;

            for (int i = 0; i < pop.size(); i++) {
                if (pop.get(i).getObjective(j) > zmax[j]) {
                    zmax[j] = pop.get(i).getObjective(j);
                }
            }
        }
        return zmax;
    }

    void normalizePopulation(List<S> pop) {

        int obj = problem_.getNumberOfObjectives();

        for (int i = 0; i < pop.size(); i++) {
            Solution sol = pop.get(i);

            double[] normalizedObjective_ = new double[obj];
            for (int j = 0; j < obj; j++) {

                double val = (sol.getObjective(j) - zideal_[j])
                        / (znadir_[j] - zideal_[j]);

                normalizedObjective_[j] = val;
            }
            sol.setAttribute("NormalizedObjective", normalizedObjective_);
        }
    }

    @Override
    public String getName() {
        return "ThetaDEA";
    }

    @Override
    public String getDescription() {
        return "A new dominance relation-based evolutionary algorithm for many-objective optimization.";
    }

    protected void initializeUniformWeight() {
        String dataFileName;
        dataFileName = "W" + problem_.getNumberOfObjectives() + "D_"
                + populationSize_ + ".dat";

        lambda_ = new double[populationSize_][problem_.getNumberOfObjectives()];

        try {
            InputStream in = getClass().getResourceAsStream("/WeightVectors/" + dataFileName);
            InputStreamReader isr = new InputStreamReader(in);
            try (BufferedReader br = new BufferedReader(isr)) {
                int i = 0;
                int j;
                String aux = br.readLine();
                while (aux != null) {
                    StringTokenizer st = new StringTokenizer(aux);
                    j = 0;
                    while (st.hasMoreTokens()) {
                        double value = new Double(st.nextToken());
                        lambda_[i][j] = value;
                        j++;
                    }
                    aux = br.readLine();
                    i++;
                }
            }
        } catch (IOException | NumberFormatException e) {
            throw new JMetalException("initializeUniformWeight: failed when reading for file: /WeightVectors/" + dataFileName, e);
        }
    }

    @Override
    public void run() {

        evaluations = 0;

        initializeUniformWeight();

        initPopulation();   // initialize the population;

        initIdealPoint();  // initialize the ideal point

        initNadirPoint();    // initialize the nadir point

        initExtremePoints(); // initialize the extreme points

        while (evaluations < maxEvaluations) {

            createOffSpringPopulation();  // create the offspring population

            union_ = new ArrayList<>();
            union_.addAll(population_);
            union_.addAll(offspringPopulation_);

            List<S>[] sets = getParetoFronts();

            List<S> firstFront = sets[0];   // the first non-dominated front
            List<S> stPopulation = sets[1]; // the population used in theta-non-dominated ranking

            updateIdealPoint(firstFront);  // update the ideal point

            if (normalize_) {
                updateNadirPoint(firstFront);  // update the nadir point
                normalizePopulation(stPopulation);  // normalize the population using ideal point and nadir point
            }

            getNextPopulation(stPopulation);  // select the next population using theta-non-dominated ranking

            evaluations += populationSize_;

        }

    }

    @Override
    public List<S> getResult() {
        return SolutionListUtils.getNondominatedSolutions(population_);
    }

}
