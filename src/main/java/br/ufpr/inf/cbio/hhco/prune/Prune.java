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
package br.ufpr.inf.cbio.hhcoanalysis.prune;

import br.ufpr.inf.cbio.hhco.algorithm.SPEA2SDE.EnvironmentalSelectionSDE;
import br.ufpr.inf.cbio.hhco.algorithm.SPEA2SDE.StrengthRawFitnessSDE;
import br.ufpr.inf.cbio.hhco.util.output.Utils;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontUtils;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 */
public class Prune {

    public enum Method {
        MINMAX, SDE, LPNORM
    };

    private final Method method;
    private final int size;
    private List population;
    private String output;

    public Prune(Method method, int size) {
        this.method = method;
        this.size = size;
    }

    public void setPopulation(String input) throws FileNotFoundException {
        setPopulation(FrontUtils.convertFrontToSolutionList(new ArrayFront(input)));
    }

    public void setPopulation(List population) {
        this.population = population;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public List prune(List population) {
        setPopulation(population);
        return prune();
    }

    public List prune() {
        List pruned = population;
        if (population.size() > size) {
            switch (method) {
                case MINMAX:
                    pruned = MOEADUtils.getSubsetOfEvenlyDistributedSolutions(population, size);
                    break;
                case SDE:
                    (new StrengthRawFitnessSDE<>()).computeDensityEstimator(population);
                    pruned = (new EnvironmentalSelectionSDE<>(size)).execute(population);
                    break;
                case LPNORM:
                    pruned = LpNormDistanceBasedPrune.getSubsetOfEvenlyDistributedSolutions(population, size);
                    break;
            }
        }
        return pruned;
    }

    public void pruneToFile() {
        List pruned = prune();
        /**
         * Create output path and file
         */
        (new Utils(Paths.get(output).getParent().toString())).prepareOutputDirectory();
        /**
         * Output solution set to file
         */
        new SolutionListOutput(pruned).setSeparator("\t")
                .setFunFileOutputContext(new DefaultFileOutputContext(output))
                .print();
    }

    public void pruneToFile(String file, List population) {
        setOutput(file);
        setPopulation(population);
        pruneToFile();
    }

    public void pruneToFile(String file) {
        setOutput(file);
        pruneToFile();
    }

    public void pruneToFile(List population) {
        setPopulation(population);
        pruneToFile();

    }

    public static Prune parse(String[] args) throws FileNotFoundException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        Options options = new Options();

        try {
            options.addOption(Option.builder("h").longOpt("help").desc("print this message and exits.").build());

            // parse command line to check help option
            cmd = parser.parse(options, args, true);

            options.addOption(Option.builder("in").longOpt("input").hasArg().argName("file").required()
                    .desc("the pareto front file to be pruned").build());
            options.addOption(Option.builder("out").longOpt("output").hasArg().argName("file").required()
                    .desc("the file to output the pruned pareto front").build());
            options.addOption(Option.builder("m").longOpt("method").hasArg().argName("[MINMAX|SDE]").required()
                    .desc("the prune method").build());
            options.addOption(Option.builder("s").longOpt("size").hasArg().argName("t").required()
                    .desc("the number of output solutions").build());

            // print help and exit
            if (cmd.hasOption("h") || args.length == 0) {
                help(options);
                System.exit(0);
            }

            // parse command line to check other options
            cmd = parser.parse(options, args);

            return build(cmd, options);

        } catch (ParseException ex) {
            help(options);
            Logger.getLogger(br.ufpr.inf.cbio.hhco.runner.Main.class.getName()).log(Level.SEVERE,
                    "Failed to parse command line arguments. Execute with -h for usage help.", ex);
        }
        return null;
    }

    public static void help(Options options) {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(
                "java -cp <jar> " + Prune.class.getCanonicalName(),
                "\nCompute performance metrics following CEC'2018 competition on many-objective optimization instructions.\n",
                options,
                "\nPlease report issues at https://github.com/fritsche/hhcoanalysis/issues", true);
    }

    private static Prune build(CommandLine cmd, Options options) throws ParseException, FileNotFoundException {
        Prune prune;
        Method method = null;
        int size = 0;
        String aux;
        if ((aux = cmd.getOptionValue("m")) != null) {
            if (aux.equals(Method.MINMAX.toString())) {
                method = Method.MINMAX;
            } else if (aux.equals(Method.SDE.toString())) {
                method = Method.SDE;
            } else if (aux.equals(Method.LPNORM.toString())) {
                method = Method.LPNORM;
            } else {
                invalidOptionError("m", options, cmd);
            }
        } else {
            invalidOptionError("m", options, cmd);
        }
        if ((aux = cmd.getOptionValue("s")) != null) {
            size = Integer.parseInt(aux);
        } else {
            invalidOptionError("s", options, cmd);
        }
        prune = new Prune(method, size);
        if ((aux = cmd.getOptionValue("in")) != null) {
            prune.setPopulation(aux);
        } else {
            invalidOptionError("in", options, cmd);
        }
        if ((aux = cmd.getOptionValue("out")) != null) {
            prune.setOutput(aux);
        } else {
            invalidOptionError("out", options, cmd);
        }
        return prune;
    }

    public static void invalidOptionError(String option, Options options, CommandLine cmd) throws ParseException {
        help(options);
        throw new ParseException("Invalid option value [" + cmd.getOptionValue(option) + "] for option: " + option);
    }

    public static void main(String[] args) throws FileNotFoundException {
        JMetalLogger.logger.setLevel(Level.CONFIG);
        Prune prune = parse(args);
        prune.pruneToFile();
    }

}
