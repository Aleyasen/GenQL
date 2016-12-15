/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genql;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

/**
 *
 * @author Amirhossein Aleyasen <aleyase2@illinois.edu>
 * created on Apr 2, 2016, 1:14:53 PM
 */
public class CLIRunner {

    private static boolean isEval = false;
    private static boolean isGen = false;

    private static String corpusDir;
    private static String outputFile;
    private static String groundTruthQueryFile;
    private static String generatedQueryFile;

    public static void main(String[] args) {
        Options options = setupOptions();
        CommandLineParser parser = new BasicParser();
        try {
            for (int i = 0; i < args.length; i++) {
                System.out.println(args[i]);
            }
            CommandLine line = parser.parse(options, args);
            if (line.hasOption('e')) {
                isEval = true;
            }
            if (line.hasOption('g')) {
                isGen = true;
            }
            if (line.hasOption('d')) {
                corpusDir = line.getOptionValue('d');
            }
            if (line.hasOption('o')) {
                outputFile = line.getOptionValue('o');
            }
            if (line.hasOption('g')) {
                groundTruthQueryFile = line.getOptionValue('g');
            }

            if (line.hasOption('q')) {
                generatedQueryFile = line.getOptionValue('q');
            }
            if (line.hasOption('h')) {
                printHelpAndExit(options);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Unexpected exception: " + ex.getMessage());
            System.exit(1);
        }
    }

    private static Options setupOptions() {
        Options options = new Options();
        options.addOption("g", "gen", false, "Generate search query log");
        options.addOption("e", "eval", false, "Evaluate generated queries based on a ground-truth query log");
        options.addOption("d", "dir", true, "The directory for the corpus");
        options.addOption("o", "output", true, "Output file for the generated queries");
        options.addOption("g", "gtruth", true, "The file for the ground-truth query log (only for evaluation). Each line of the file contains a query");
        options.addOption("q", "query", true, "The file for the generated queries using -g option (only for evaluation)");
        options.addOption("h", "help", false, "help");

//        options.addOption("o", "output", true, "Output file/directory path");
//        options.addOption("o", "output", true, "Output file/directory path");
        return options;
    }

    private static void printHelpAndExit(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("genql.jar", options, true);
        System.exit(1);
    }

}
