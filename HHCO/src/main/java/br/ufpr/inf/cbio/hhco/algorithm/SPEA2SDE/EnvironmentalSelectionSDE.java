/*
 * Copyright (C) 2018 Gian Fritsche <gian.fritsche at gmail.com>
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
package br.ufpr.inf.cbio.hhco.algorithm.SPEA2SDE;

import org.apache.commons.lang3.tuple.Pair;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.solutionattribute.impl.LocationAttribute;

import java.util.*;

public class EnvironmentalSelectionSDE<S extends Solution<?>> implements SelectionOperator<List<S>,List<S>> {

  private int solutionsToSelect = 0;
  private final StrengthRawFitnessSDE<S> strengthRawFitness= new StrengthRawFitnessSDE<>();

  public EnvironmentalSelectionSDE(int solutionsToSelect) {
    this.solutionsToSelect = solutionsToSelect;
  }

  @Override
  public List<S> execute(List<S> source2) {
    int size;
    List<S> source = new ArrayList<>(source2.size());
    source.addAll(source2);
    if (source2.size() < this.solutionsToSelect) {
      size = source.size();
    } else {
      size = this.solutionsToSelect;
    }

    List<S> aux = new ArrayList<>(source.size());

    int i = 0;
    while (i < source.size()){
      double fitness = (double) this.strengthRawFitness.getAttribute(source.get(i));
      if (fitness<1.0){
        aux.add(source.get(i));
        source.remove(i);
      } else {
        i++;
      }
    }

    if (aux.size() < size){
      StrengthFitnessComparatorSDE<S> comparator = new StrengthFitnessComparatorSDE<>();
      Collections.sort(source,comparator);
      int remain = size - aux.size();
      for (i = 0; i < remain; i++){
        aux.add(source.get(i));
      }
      return aux;
    } else if (aux.size() == size) {
      return aux;
    }

    double [][] distance = SPEA2SDEUtils.distanceMatrixSDE(aux);
    List<List<Pair<Integer, Double>> > distanceList = new ArrayList<>();
    LocationAttribute<S> location = new LocationAttribute<>(aux);
    for (int pos = 0; pos < aux.size(); pos++) {
      List<Pair<Integer, Double>> distanceNodeList = new ArrayList<>();
      for (int ref = 0; ref < aux.size(); ref++) {
        if (pos != ref) {
          distanceNodeList.add(Pair.of(ref, distance[pos][ref]));
        }
      }
      distanceList.add(distanceNodeList);
    }


    for (int q = 0; q < distanceList.size(); q++){
      Collections.sort(distanceList.get(q),new Comparator<Pair<Integer, Double>> () {
        @Override
        public int compare(Pair<Integer, Double> pair1, Pair<Integer, Double> pair2) {
          if (pair1.getRight()  < pair2.getRight()) {
            return -1;
          } else if (pair1.getRight()  > pair2.getRight()) {
            return 1;
          } else {
            return 0;
          }
        }
      });

    }

    while (aux.size() > size) {
      double minDistance = Double.MAX_VALUE;
      int toRemove = 0;
      i = 0;
      Iterator<List<Pair<Integer, Double>>> iterator = distanceList.iterator();
      while (iterator.hasNext()){
        List<Pair<Integer, Double>> dn = iterator.next();
        if (dn.get(0).getRight() < minDistance){
          toRemove = i;
          minDistance = dn.get(0).getRight();
          //i y toRemove have the same distance to the first solution
        } else if (dn.get(0).getRight() == minDistance) {
          int k = 0;
          while ((dn.get(k).getRight() ==
              distanceList.get(toRemove).get(k).getRight()) &&
              k < (distanceList.get(i).size()-1)) {
            k++;
          }

          if (dn.get(k).getRight() <
              distanceList.get(toRemove).get(k).getRight()) {
            toRemove = i;
          }
        }
        i++;
      }

      int tmp =  (int) location.getAttribute(aux.get(toRemove));
      aux.remove(toRemove);
      distanceList.remove(toRemove);

      Iterator<List<Pair<Integer, Double>>> externIterator = distanceList.iterator();
      while (externIterator.hasNext()){
        Iterator<Pair<Integer, Double>> interIterator = externIterator.next().iterator();
        while (interIterator.hasNext()){
          if (interIterator.next().getLeft() == tmp){
            interIterator.remove();
            continue;
          }
        }
      }
    }
    return aux;
  }

}
