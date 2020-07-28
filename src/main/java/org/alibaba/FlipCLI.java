package org.alibaba;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FlipCLI {
    public static final String DEFAULT_SOURCE_FILE_PATH = "/usr/share/dict/linux.words";
    public static final String DEFAULT_GRAPH_FILE_PATH = "/tmp/linux.words.graph";
    private static final String HELP_OPT = "-h";
    private static final String HELP_LONG_OPT = "--help";

    private final static String usage = "Usage: bin/flip -p [flip|parse] [OPTIONS]\n"
            + "The following processors are available:\n"
            + "  * PARSE: Parse word dictionary file into word flip graph.\n"
            + "  * FLIP: According to the word flip graph, find the shortest flip path of two words.\n"
            + "\n"
            + "Required command line arguments:\n"
            + "-p,--processor <arg>   Specifies the type of task to be processed.\n"
            + "\n"
            + "Optional command line arguments:\n"
            + "-s,--sourceFile <arg>  Word dictionary file path.\n"
            + "                       DEFAULT: /usr/share/dict/linux.words\n"
            + "-g,--graphFile <arg>   Word flip graph file path.\n"
            + "                       DEFAULT: /tmp/linux.words.graph\n"
            + "-f,--fromWord <arg>    Start of flip path.\n"
            + "-t,--toWord <arg>      End of flip path.\n"
            + "-h,--help              Display usage information and exit.\n";

    private static Options buildOptions() {
        Options options = new Options();

        OptionBuilder.isRequired();
        OptionBuilder.hasArgs();
        OptionBuilder.withLongOpt("processor");
        options.addOption(OptionBuilder.create("p"));
        options.addOption("g", "graphFile", true, "");
        options.addOption("s", "sourceFile", true, "");
        options.addOption("f", "fromWord", true, "");
        options.addOption("t", "toWord", true, "");
        options.addOption("h", "help", false, "");

        return options;
    }

    private static void printUsage() {
        System.out.println(usage);
    }

    private static boolean isHelpOption(String arg) {
        return arg.equalsIgnoreCase(HELP_OPT) ||
                arg.equalsIgnoreCase(HELP_LONG_OPT);
    }

    public static void main(String[] args) {
        Options options = buildOptions();
        if (args.length == 0) {
            printUsage();
            System.exit(0);
        }
        if (args.length == 1 && isHelpOption(args[0])) {
            printUsage();
            System.exit(0);
        }
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("Error parsing command-line options: ");
            printUsage();
            System.exit(-1);
        }

        WordGraph wordGraph = new WordGraph();
        String graphFileStr = cmd.getOptionValue("g", DEFAULT_GRAPH_FILE_PATH);
        String processor = cmd.getOptionValue("p", "flip");
        switch (processor.toUpperCase()) {
            case "FLIP":
                String fromWord = cmd.getOptionValue("f");
                String toWord = cmd.getOptionValue("t");
                if (fromWord == null || toWord == null) {
                    System.out.println("Exception: [fromWord] and [toWord] are required for FLIP processor!");
                    printUsage();
                    System.exit(-1);
                }

                File graphFile = new File(graphFileStr);
                if (!graphFile.exists()) {
                    System.out.println("Exception: " + graphFileStr + " file does not exist!");
                    printUsage();
                    System.exit(-1);
                }

                try {
                    wordGraph.loadParsed(graphFileStr);
                } catch (IOException e) {
                    System.out.println("FAILURE load from graph file: " + graphFileStr + " !");
                    System.out.println("Exception: " + e.getMessage());
                    System.exit(-1);
                }
                List<String> flipPath = wordGraph.getFlipPath(fromWord, toWord);
                if (flipPath == null) {
                    System.out.println("There is no flip path from " + fromWord + " to " + toWord + "!");
                } else {
                    System.out.println("The shortest flip path from " + fromWord + " to " + toWord + ":");
                    System.out.println(flipPath);
                }
                break;
            case "PARSE":
                String sourceFileStr = cmd.getOptionValue("s", DEFAULT_SOURCE_FILE_PATH);
                try {
                    wordGraph.parseSource(sourceFileStr, graphFileStr);
                } catch (IOException e) {
                    System.out.println("FAILURE parse " + sourceFileStr + " to " + graphFileStr + " !");
                    System.out.println("Exception: " + e.getMessage());
                    System.exit(-1);
                }
                System.out.println("SUCCESS parse " + sourceFileStr + " to " + graphFileStr + " !");
                break;
        }
        System.exit(0);
    }
}
