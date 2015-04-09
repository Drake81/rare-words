package main;

import computations.ReduceWordlist;
import computations.WordCounter;
import model.Options;
import model.SentenceList;
import similarity.SimilarityMatrix;
import sql.SqlObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Main class for the computations of all algorithms
 *
 * @author Karsten Brandt
 * @author Martin Stoffers
 * @author Lukas Fischer
 */
public class Main {

    /**
     * Executes all algorithm's in a predefined order
     * Sets up some global object, like property  and logger object
     * <p>
     * Please make sure, that credentials.properties an logging.properties are setup up correctly
     *
     * @param args commandline arguments
     */
    public static void main(String[] args) {
        try {

            System.setProperty("java.util.logging.config.file", "logs/logging.properties");
            LogManager.getLogManager().readConfiguration();
            final Logger log = Logger.getLogger(Main.class.getName());

            try {

                log.fine("read configs");
                FileInputStream file =  new FileInputStream("credentials.properties");
                BufferedInputStream stream = new BufferedInputStream(file);
                Properties properties = new Properties();
                properties.load(stream);
                stream.close();

                Options opt = null;
                try {
                    opt = new Options(properties);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }

                //Database
                String server = properties.getProperty("server");
                int port = Integer.parseInt(properties.getProperty("port"));
                String user = properties.getProperty("user");
                String pass = properties.getProperty("pass");
                String[] databases = properties.getProperty("databases").replace(" ", "").split(",");

                for (String database : databases) {
                    try {

                        // create Database object
                        SqlObject currentdatabase = new SqlObject(server, port, user, pass, database, opt);
                        SentenceList sentenceList = null;
                        WordCounter wordcounter = null;
                        HashMap<String, Double> wordlist = null;
                        SimilarityMatrix similarityMatrix = null;


                        currentdatabase.prepare();

                        if (opt.isUpdateWordCounts()) {
                            System.out.println();

                            if (!currentdatabase.isTableEmpty("sentences")) {
                                System.out.println("Get sentences from database " + database);
                                sentenceList = currentdatabase.getSentences();
                            } else {
                                System.out.println("Database " + database + " has no entries in table sentences - Skipping");
                                System.exit(1);
                            }
                            if (!currentdatabase.isTableEmpty("word_frequency")) {
                                System.out.println("Cleaning word_frequency table in database " + database);
                                currentdatabase.cleanTable("word_frequency");
                            }
                            long startTime = System.currentTimeMillis();
                            wordcounter = new WordCounter(sentenceList, opt);
                            long endTime = System.currentTimeMillis();
                            log.config("Word count calculation took " + (endTime-startTime) + " ms.");

                            System.out.println("Populate word_frequency table in database " + database + ". Be patient...");
                            startTime = System.currentTimeMillis();
                            currentdatabase.insertWordCount(wordcounter.getWordCounts());
                            endTime = System.currentTimeMillis();
                            log.config("Populating to word_count table took " + (endTime-startTime) + " ms.");
                        }

                        //Build similarity matrix
                        if (opt.isUpdateSimilarities()) {
                            System.out.println();

                            // reduce word list
                            if (!currentdatabase.isTableEmpty("word_frequency")) {
                                long startTime = System.currentTimeMillis();
                                if (opt.isPercent()) {
                                    System.out.println("Reduce word list to " + opt.getPercentValue());
                                    wordlist = ReduceWordlist.confidenceFunction(currentdatabase, opt.getPercentValue());
                                } else if (opt.isByRange()) {
                                    System.out.println("Reduce word list by frequency. Low:" + opt.getLowerLimit() + " High:" + opt.getHigherLimit());
                                    wordlist = ReduceWordlist.absoluteValues(currentdatabase, opt.getLowerLimit(), opt.getHigherLimit());
                                }
                                long endTime = System.currentTimeMillis();
                                log.info("Reduce to rare words took " + (endTime-startTime) + " ms.");
                            } else {
                                System.out.println("Database " + database + " has no entries in table word_frequency - skipping");
                                System.exit(1);
                            }

                            // get sentenceslist if needed
                            if(sentenceList == null) {
                                if (!currentdatabase.isTableEmpty("sentences")) {
                                    System.out.println("Get sentences from database " + database + "\n");
                                    sentenceList = currentdatabase.getSentences();
                                }
                                else {
                                    System.out.println("Database " + database + " has no table sentences - Skipping");
                                    System.exit(1);
                                }
                            }

                            // clear table
                            if (!currentdatabase.isTableEmpty("sentence_similarity")) {
                                System.out.println("Cleaning sentence_similarity table in database " + database);
                                currentdatabase.cleanTable("sentence_similarity");
                            }

                            if (opt.isByRange() || opt.isPercent()) {
                                System.out.println("Calculating similarities for " + database + " with RareWords in mind... ");
                                similarityMatrix = new SimilarityMatrix(sentenceList, wordlist, opt);
                            } else {
                                System.out.println("Calculating similarities for " + database);
                                similarityMatrix = new SimilarityMatrix(sentenceList, opt);
                            }
                            long startTime = System.currentTimeMillis();
                            similarityMatrix.calculateSimilarity();
                            long endTime = System.currentTimeMillis();
                            log.info("Similarity calculation took " + (endTime-startTime) + " ms.");

                            if(similarityMatrix.getSimilarityMatrix().size() > 0) {
                                System.out.println("Populate sentence_similarity table in database " + database + ". Be patient...");
                                startTime = System.currentTimeMillis();
                                currentdatabase.insertSentenceSimilarities(similarityMatrix);
                                endTime = System.currentTimeMillis();
                                log.info("Populating to similarity table took " + (endTime-startTime) + " ms.");
                            }
                            else {
                                System.out.println("There is nothing to commit");
                            }
                            System.out.println();
                        }

                    } catch (SQLException e) {
                        log.warning(e.toString());
                        System.exit(1);
                    }
                    if(!opt.isUpdateWordCounts() && !opt.isUpdateSimilarities()) {
                        System.out.println("There is nothing todo.");
                    }
                    else {
                        System.out.println("Bye");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error while reading general properties file");
                log.warning(e.toString());
                System.exit(1);
            }
        } catch (IOException e) {
            System.out.println("Error while reading logging file");
            System.exit(1);
        }
    }
}
