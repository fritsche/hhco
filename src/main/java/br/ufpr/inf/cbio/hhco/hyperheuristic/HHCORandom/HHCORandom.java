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
package br.ufpr.inf.cbio.hhco.hyperheuristic.HHCORandom;

import br.ufpr.inf.cbio.hhco.hyperheuristic.CooperativeAlgorithm;
import br.ufpr.inf.cbio.hhco.hyperheuristic.HHCO.HHCO;
import br.ufpr.inf.cbio.hhco.metrics.fir.VoidFIR;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalLogger;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 * @param <S>
 */
public class HHCORandom<S extends Solution<?>> extends HHCO<S> {

    private CooperativeAlgorithm<S> selected;

    @Override
    public CooperativeAlgorithm<S> getSelected() {
        return selected;
    }

    public HHCORandom(List<CooperativeAlgorithm<S>> algorithms, int populationSize, int maxEvaluations, Problem problem) {
        super(algorithms, populationSize, maxEvaluations, problem, "HHCORandom", new RandomSelection<CooperativeAlgorithm>(), new VoidFIR());
    }

    @Override
    public void run() {

        setEvaluations(0);
        for (CooperativeAlgorithm<S> alg : getAlgorithms()) {
            alg.init(populationSize);
            setEvaluations(getEvaluations() + alg.getPopulation().size());
            selection.add(alg);
        }
        selection.init();

        while (getEvaluations() < getMaxEvaluations()) {

            JMetalLogger.logger.log(Level.FINE, "Progress: {0}",
                    String.format("%.2f%%", getEvaluations() / (double) getMaxEvaluations() * 100.0));

            // heuristic selection
            selected = selection.getNext(getEvaluations() / populationSize);

            // apply selected heuristic
            selected.doIteration();
            // copy the solutions generatedy by selected
            List<S> offspring = new ArrayList<>();
            for (S s : selected.getOffspring()) {
                offspring.add((S) s.copy());
                // count evaluations used by selected
                setEvaluations(getEvaluations() + 1);
            }

            // cooperation phase
            for (CooperativeAlgorithm<S> neighbor : getAlgorithms()) {
                if (neighbor != selected) {
                    List<S> migrants = new ArrayList<>();
                    for (S s : offspring) {
                        migrants.add((S) s.copy());
                    }
                    neighbor.receive(migrants);
                }
            }

            // notify observers
            setChanged();
            notifyObservers();

        }

    }

}
