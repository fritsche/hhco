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
package br.ufpr.inf.cbio.hhco.algorithm.NSGAIII;

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
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

/**
 * Based on the source distributed by Yuan Yuan
 * (https://github.com/yyxhdy/ManyEAs) <yyxhdy at gmail.com>
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 * @param <S>
 */
public class NSGAIII<S extends Solution> implements Algorithm<List<S>> {

    protected int populationSize_;

    protected List<S> population_;
    List<S> offspringPopulation_;
    List<S> union_;

    int evaluations;

    CrossoverOperator<S> crossover_;
    MutationOperator<S> mutation_;
    SelectionOperator<List<S>, S> selection_;

    double[][] lambda_; // reference points

    boolean normalize_; // do normalization or not

    protected final Problem<S> problem_;

    protected int maxEvaluations;

    public NSGAIII(NSGAIIIBuilder<S> builder) {

        problem_ = builder.getProblem();

        maxEvaluations = builder.getMaxEvaluations();

        normalize_ = builder.getNormalize();

        populationSize_ = builder.getPopulationSize();

        mutation_ = builder.getMutation();

        crossover_ = builder.getCrossover();

        selection_ = builder.getSelection();

    } // NSGAII

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

        if (populationSize_ % 2 != 0) {
            populationSize_ += 1;
        }

        initPopulation();
        evaluations += populationSize_;

        while (evaluations < maxEvaluations) {
            offspringPopulation_ = new ArrayList<>(populationSize_);
            for (int i = 0; i < (populationSize_ / 2); i++) {
                if (evaluations < maxEvaluations) {
                    // obtain parents

                    List<S> parents = new ArrayList<>();
                    parents.add(selection_.execute(population_));
                    parents.add(selection_.execute(population_));

                    List<S> offSpring = crossover_.execute(parents);

                    mutation_.execute(offSpring.get(0));
                    mutation_.execute(offSpring.get(1));

                    problem_.evaluate(offSpring.get(0));
                    problem_.evaluate(offSpring.get(1));
                    evaluations += 2;

                    offspringPopulation_.add(offSpring.get(0));
                    offspringPopulation_.add(offSpring.get(1));

                } // if
            } // for

            union_ = new ArrayList<>();
            union_.addAll(population_);
            union_.addAll(offspringPopulation_);

            // Ranking the union
            Ranking ranking = (new DominanceRanking()).computeRanking(union_);

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
            }

            if (remain > 0) { // front contains individuals to insert
                new Niching(population_, front, lambda_, remain, normalize_)
                        .execute();
            }

        }

    }

    void initPopulation() {

        population_ = new ArrayList<>(populationSize_);

        for (int i = 0; i < populationSize_; i++) {
            S newSolution = problem_.createSolution();
            problem_.evaluate(newSolution);
            population_.add(newSolution);
        }

    }

    @Override
    public List<S> getResult() {
        return SolutionListUtils.getNondominatedSolutions(population_);
    }

    @Override
    public String getName() {
        return "NSGAIII";
    }

    @Override
    public String getDescription() {
        return "\"unofficial\" implementation of NSGA-III";
    }

}
