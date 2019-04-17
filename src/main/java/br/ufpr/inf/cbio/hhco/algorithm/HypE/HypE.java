/*
 * Copyright (C) 2018 Gian Fritsche <gian.fritsche@gmail.com>
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
package br.ufpr.inf.cbio.hhco.algorithm.HypE;

import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;
import org.uma.jmetal.util.solutionattribute.impl.Fitness;

/**
 * Based on the source received from Lei Cai <caileid at gmail.com>
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 * @param <S>
 */
public class HypE<S extends Solution<?>> implements Algorithm<List<S>> {

    int populationSize;
    private final int maxEvaluations;
    int samples;
    int evaluations;
    List<S> population;
    List<S> offspringPopulation;
    HypEFitnessAssignment fs = new HypEFitnessAssignment();
    S reference;
    List<S> union;
    CrossoverOperator<S> crossoverOperator;
    MutationOperator<S> mutationOperator;
    SelectionOperator<List<S>, S> selectionOperator;
    protected final Problem<S> problem;
    final Fitness fitness = new Fitness();

    public HypE(HypEBuilder<S> builder) {
        problem = builder.getProblem();
        populationSize = builder.getPopulationSize();
        maxEvaluations = builder.getMaxEvaluations();
        samples = builder.getSamples();
        crossoverOperator = builder.getCrossoverOperator();
        mutationOperator = builder.getMutationOperator();
        selectionOperator = builder.getSelectionOperator();
    }

    public void environmentalSelection() {
        union = new ArrayList<>();
        union.addAll(population);
        union.addAll(offspringPopulation);

        // Ranking the union
        Ranking ranking = (new DominanceRanking()).computeRanking(union);

        int remain = populationSize;
        int index = 0;
        List<S> front;
        population.clear();

        // Obtain the next front
        front = ranking.getSubfront(index);

        while ((remain > 0) && (remain >= front.size())) {

            for (int k = 0; k < front.size(); k++) {
                population.add(front.get(k));
            }
            // Decrement remain
            remain = remain - front.size();
            // Obtain the next front
            index++;
            if (remain > 0) {
                front = ranking.getSubfront(index);
            } // if
        }
        if (remain > 0) { // front contains individuals to insert
            int k = front.size() - remain;
            while (k > 0) {
                fs.setHypEFitness(front, reference, k, samples);
                int loc = -1;
                double min = Double.MAX_VALUE;
                for (int u = 0; u < front.size(); u++) {
                    if ((Double) fitness.getAttribute(front.get(u)) < min) {
                        min = (Double) fitness.getAttribute(front.get(u));
                        loc = u;
                    }
                }
                front.remove(loc);
                k--;
            }
            for (int u = 0; u < front.size(); u++) {
                population.add(front.get(u));
            }
        }
    }

    @Override
    public void run() {

        if (populationSize % 2 != 0) {
            populationSize += 1;
        }
        population = new ArrayList<>(populationSize);
        reference = problem.createSolution();
        evaluations = 0;
        for (int i = 0; i < populationSize; i++) {
            S newSolution = problem.createSolution();
            problem.evaluate(newSolution);
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                if (newSolution.getObjective(j) > reference.getObjective(j)) {
                    reference.setObjective(j, newSolution.getObjective(j));
                }
            }
            evaluations++;
            population.add(newSolution);
        }
        for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
            reference.setObjective(j, reference.getObjective(j) * 1.2);
        }

        while (evaluations < maxEvaluations) {

            offspringPopulation = new ArrayList<>(populationSize);

            fs.setHypEFitness(population, reference, populationSize, samples);
            for (int i = 0; i < (populationSize / 2); i++) {
                if (evaluations < maxEvaluations) {
                    List<S> parents = new ArrayList<>();

                    parents.add(selectionOperator.execute(population));
                    parents.add(selectionOperator.execute(population));

                    List<S> offSpring = crossoverOperator.execute(parents);

                    mutationOperator.execute(offSpring.get(0));
                    mutationOperator.execute(offSpring.get(1));

                    problem.evaluate(offSpring.get(0));
                    problem.evaluate(offSpring.get(1));
                    evaluations += 2;
                    offspringPopulation.add(offSpring.get(1));
                    offspringPopulation.add(offSpring.get(0));
                }
            }

            environmentalSelection();
        }

    }

    @Override
    public List<S> getResult() {
        return SolutionListUtils.getNondominatedSolutions(population);
    }

    @Override
    public String getName() {
        return "HypE";
    }

    @Override
    public String getDescription() {
        return "An Algorithm for Fast Hypervolume-Based Many-Objective Optimization";
    }

    public void print(S s) {
        for (int i = 0; i < s.getNumberOfVariables(); i++) {
            System.out.print(s.getVariableValue(i) + " ");
        }
        System.out.println();
        for (int i = 0; i < s.getNumberOfObjectives(); i++) {
            System.out.print(s.getObjective(i) + " ");
        }
        System.out.println("\n" + s.getAttribute(fitness.getAttributeIdentifier()));
    }

}
