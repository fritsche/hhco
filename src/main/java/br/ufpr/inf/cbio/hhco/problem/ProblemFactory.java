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
package br.ufpr.inf.cbio.hhco.problem;

import br.ufpr.inf.cbio.hhco.problem.MaF.MaF01;
import br.ufpr.inf.cbio.hhco.problem.MaF.MaF02;
import br.ufpr.inf.cbio.hhco.problem.MaF.MaF03;
import br.ufpr.inf.cbio.hhco.problem.MaF.MaF04;
import br.ufpr.inf.cbio.hhco.problem.MaF.MaF05;
import br.ufpr.inf.cbio.hhco.problem.MaF.MaF06;
import br.ufpr.inf.cbio.hhco.problem.MaF.MaF07;
import br.ufpr.inf.cbio.hhco.problem.MaF.MaF08;
import br.ufpr.inf.cbio.hhco.problem.MaF.MaF09;
import br.ufpr.inf.cbio.hhco.problem.MaF.MaF10;
import br.ufpr.inf.cbio.hhco.problem.MaF.MaF11;
import br.ufpr.inf.cbio.hhco.problem.MaF.MaF12;
import br.ufpr.inf.cbio.hhco.problem.MaF.MaF13;
import br.ufpr.inf.cbio.hhco.problem.MaF.MaF14;
import br.ufpr.inf.cbio.hhco.problem.MaF.MaF15;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.dtlz.DTLZ1;
import org.uma.jmetal.problem.multiobjective.dtlz.DTLZ2;
import org.uma.jmetal.problem.multiobjective.dtlz.DTLZ3;
import org.uma.jmetal.problem.multiobjective.dtlz.DTLZ4;
import org.uma.jmetal.problem.multiobjective.dtlz.DTLZ5;
import org.uma.jmetal.problem.multiobjective.dtlz.DTLZ6;
import org.uma.jmetal.problem.multiobjective.dtlz.DTLZ7;
import org.uma.jmetal.problem.multiobjective.wfg.WFG1;
import org.uma.jmetal.problem.multiobjective.wfg.WFG2;
import org.uma.jmetal.problem.multiobjective.wfg.WFG3;
import org.uma.jmetal.problem.multiobjective.wfg.WFG4;
import org.uma.jmetal.problem.multiobjective.wfg.WFG5;
import org.uma.jmetal.problem.multiobjective.wfg.WFG6;
import org.uma.jmetal.problem.multiobjective.wfg.WFG7;
import org.uma.jmetal.problem.multiobjective.wfg.WFG8;
import org.uma.jmetal.problem.multiobjective.wfg.WFG9;
import org.uma.jmetal.util.JMetalException;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 */
public class ProblemFactory {

    public static Problem getProblem(String problem, int m) {

        if (problem.startsWith("Minus")) {
            return new InvertedProblem((DoubleProblem) getProblem(problem.substring(5), m), problem);
        }

        int k, l, d;
        switch (problem) {
            case "DTLZ1":
                k = 5;
                return new DTLZ1(m + k - 1, m);
            case "DTLZ2":
                k = 10;
                return new DTLZ2(m + k - 1, m);
            case "DTLZ3":
                k = 10;
                return new DTLZ3(m + k - 1, m);
            case "DTLZ4":
                k = 10;
                return new DTLZ4(m + k - 1, m);
            case "DTLZ5":
                k = 10;
                return new DTLZ5(m + k - 1, m);
            case "DTLZ6":
                k = 10;
                return new DTLZ6(m + k - 1, m);
            case "DTLZ7":
                k = 10;
                return new DTLZ7(m + k - 1, m);
            case "WFG1":
                k = 2 * (m - 1);
                return new WFG1(k, 20, m);
            case "WFG2":
                k = 2 * (m - 1);
                return new WFG2(k, 20, m);
            case "WFG3":
                k = 2 * (m - 1);
                return new WFG3(k, 20, m);
            case "WFG4":
                k = 2 * (m - 1);
                return new WFG4(k, 20, m);
            case "WFG5":
                k = 2 * (m - 1);
                return new WFG5(k, 20, m);
            case "WFG6":
                k = 2 * (m - 1);
                return new WFG6(k, 20, m);
            case "WFG7":
                k = 2 * (m - 1);
                return new WFG7(k, 20, m);
            case "WFG8":
                k = 2 * (m - 1);
                return new WFG8(k, 20, m);
            case "WFG9":
                k = 2 * (m - 1);
                return new WFG9(k, 20, m);
            case "MaF01":
                k = 10;
                d = m + k - 1;
                return new MaF01(d, m);
            case "MaF02":
                k = 10;
                d = m + k - 1;
                return new MaF02(d, m);
            case "MaF03":
                k = 10;
                d = m + k - 1;
                return new MaF03(d, m);
            case "MaF04":
                k = 10;
                d = m + k - 1;
                return new MaF04(d, m);
            case "MaF05":
                k = 10;
                d = m + k - 1;
                return new MaF05(d, m);
            case "MaF06":
                k = 10;
                d = m + k - 1;
                return new MaF06(d, m);
            case "MaF07":
                k = 20;
                d = m + k - 1;
                return new MaF07(d, m);
            case "MaF08":
                d = 2;
                return new MaF08(d, m);
            case "MaF09":
                d = 2;
                return new MaF09(d, m);
            case "MaF10":
                k = m - 1;
                l = 10;
                d = k + l;
                return new MaF10(d, m);
            case "MaF11":
                k = m - 1;
                l = 10;
                d = k + l;
                return new MaF11(d, m);
            case "MaF12":
                k = m - 1;
                l = 10;
                d = k + l;
                return new MaF12(d, m);
            case "MaF13":
                d = 5;
                return new MaF13(d, m);
            case "MaF14":
                d = m * 20;
                return new MaF14(d, m);
            case "MaF15":
                d = m * 20;
                return new MaF15(d, m);
            default:
                throw new JMetalException("There is no configurations for " + problem + " problem");
        }
    }
}
