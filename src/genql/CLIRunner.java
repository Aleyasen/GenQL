/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genql;

import edu.toronto.cs.xcurator.cli.config.RunConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Amirhossein Aleyasen <aleyase2@illinois.edu>
 * created on Apr 2, 2016, 1:14:53 PM
 */
public class CLIRunner {

    private static boolean serializeMapping = false;
    private static String mappingFilename;
    private static String tdbDirectory;
    private static String domain;
    private static String dirLocation;
    private static String fileLocation;
    private static String steps;
    private static DocumentBuilder builder;
    private static String fileType;


    public static void main(String[] args) {
        Options options = setupOptions();
        CommandLineParser parser = new BasicParser();
        try {
            for (int i = 0; i < args.length; i++) {
                System.out.println(args[i]);
            }
            CommandLine line = parser.parse(options, args);
            if (line.hasOption('t')) {
                fileType = line.getOptionValue('t');
            } else {
                fileType = XML;
            }
            if (line.hasOption('s')) {
                steps = line.getOptionValue('s');
                steps = "B" + steps;
            } else {
                steps = "BDIOFK";
            }
            if (line.hasOption('o')) {
                tdbDirectory = line.getOptionValue('o');
                File d = new File(tdbDirectory);
                if (!d.exists() || !d.isDirectory()) {
                    throw new Exception("TDB directory does not exist, please create.");
                }
            }
            if (line.hasOption('h')) {
                domain = line.getOptionValue('h');
                try {
                    URL url = new URL(domain);
                } catch (MalformedURLException ex) {
                    throw new Exception("The domain name is ill-formed");
                }
            } else {
                domain = "http://xcurator.com";
            }
            if (line.hasOption('m')) {
                serializeMapping = true;
                mappingFilename = line.getOptionValue('m');
            }
            if (line.hasOption('d')) {
                dirLocation = line.getOptionValue('d');
                inputStreams = new ArrayList<>();
                final List<String> files = Util.getFiles(dirLocation);
                for (String inputfile : files) {
                    File f = new File(inputfile);
                    if (f.isFile() && f.exists()) {
                        System.out.println("Adding document to mapping discoverer: " + inputfile);
                        inputStreams.add(new FileInputStream(f));
                    } // If it is a URL download link for the document from SEC
                    else if (inputfile.startsWith("http") && inputfile.contains("://")) {
                        // Download
                        System.out.println("Adding remote document to mapping discoverer: " + inputfile);
                        try {
                            URL url = new URL(inputfile);
                            InputStream remoteDocumentStream = url.openStream();
                            inputStreams.add(remoteDocumentStream);
                        } catch (MalformedURLException ex) {
                            throw new Exception("The document URL is ill-formed: " + inputfile);
                        } catch (IOException ex) {
                            throw new Exception("Error in downloading remote document: " + inputfile);
                        }
                    } else {
                        throw new Exception("Cannot open XBRL document: " + f.getName());
                    }
                }
            }

            if (line.hasOption('f')) {
                fileLocation = line.getOptionValue('f');
                inputStreams = new ArrayList<>();
                File f = new File(fileLocation);
                if (f.isFile() && f.exists()) {
                    System.out.println("Adding document to mapping discoverer: " + fileLocation);
                    inputStreams.add(new FileInputStream(f));
                } // If it is a URL download link for the document from SEC
                else if (fileLocation.startsWith("http") && fileLocation.contains("://")) {
                    // Download
                    System.out.println("Adding remote document to mapping discoverer: " + fileLocation);
                    try {
                        URL url = new URL(fileLocation);
                        InputStream remoteDocumentStream = url.openStream();
                        inputStreams.add(remoteDocumentStream);
                    } catch (MalformedURLException ex) {
                        throw new Exception("The document URL is ill-formed: " + fileLocation);
                    } catch (IOException ex) {
                        throw new Exception("Error in downloading remote document: " + fileLocation);
                    }
                } else {

                    throw new Exception("Cannot open XBRL document: " + f.getName());
                }

            }

            setupDocumentBuilder();
            RdfFactory rdfFactory = new RdfFactory(new RunConfig(domain));
            List<Document> documents = new ArrayList<>();
            for (InputStream inputStream : inputStreams) {
                Document dataDocument = null;
                if (fileType.equals(JSON)) {
                    String json = IOUtils.toString(inputStream);
                    final String xml = Util.json2xml(json);
                    FileUtils.writeStringToFile(new File(fileLocation + ".xml"), xml);
                    final InputStream xmlInputStream = IOUtils.toInputStream(xml);
                    dataDocument = createDocument(xmlInputStream);
                } else {
                    dataDocument = createDocument(inputStream);
                }
                documents.add(dataDocument);
            }
            if (serializeMapping) {
                System.out.println("Mapping file will be saved to: " + new File(mappingFilename).getAbsolutePath());
                rdfFactory.createRdfs(documents, tdbDirectory, mappingFilename, steps);
            } else {
                rdfFactory.createRdfs(documents, tdbDirectory, steps);
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
