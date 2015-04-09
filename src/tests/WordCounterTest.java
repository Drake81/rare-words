package tests;

import computations.WordCounter;
import model.Options;
import model.SentenceList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sql.SqlObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class WordCounterTest {

    private SqlObject db_test = null;
    private Options opt = null;
    private SentenceList sentenceList = null;

    private String server = null;
    private int port = 3306;
    private String user = null;
    private String pass = null;
    private String testdb = null;


    @Before
    public void setUp() throws Exception {
        FileInputStream file =  new FileInputStream("credentials.properties");
        BufferedInputStream stream = new BufferedInputStream(file);
        Properties properties = new Properties();
        properties.load(stream);
        stream.close();
        opt = new Options(properties);

        server = properties.getProperty("server");
        port = Integer.parseInt(properties.getProperty("port"));
        user = properties.getProperty("user");
        pass = properties.getProperty("pass");
        testdb = properties.getProperty("testdb_unit_tests");

        db_test = new SqlObject(server, port, user, pass, testdb, opt);
        sentenceList = db_test.getSentences(1);

    }

    @After
    public void tearDown() throws Exception {
        db_test.close();
    }

    @Test
    public void testGetWordCounts() throws Exception {
/*
        Sentence s = sentenceList.get(0);

        WordCounter wc = new WordCounter(sentenceList);
        HashMap<String,Long> hm = wc.getWordCounts();
        assertEquals("Should be 18 on TestDB on test", s.size(), hm.size());

        for (long count: hm.values()) {
            assertEquals("Should be 1", 1L, count);
        }

        //sentenceList = null;
        //wc = new WordCounter(sentenceList);
*/
    }

    @Test
    public void testfullWordCounter() throws Exception {

        //final int MAX_COUNT = 2000;

        // create Database object
        HashMap<String, Long> wordList = null;
        int wordcount = 0;

        // Get sentences
        if(!db_test.isTableEmpty("sentences")){
            //SentenceList slist = db_test.getSentences(MAX_COUNT);
            SentenceList slist = db_test.getSentences();

            WordCounter wordcounter = new WordCounter(slist, opt);

            wordList = wordcounter.getWordCounts();

            db_test.cleanTable("word_frequency");
            db_test.insertWordCount(wordList);
        }

        // if not empty get results
        if(!db_test.isTableEmpty("word_frequency")){
            ResultSet resultset = db_test.executeQuery("SELECT * FROM word_frequency");

            HashMap<String,Long> hp = new HashMap<String,Long>();
            while (resultset.next()) {
                wordcount++;
                hp.put(resultset.getString(1),1L);
            }

            for (String m: wordList.keySet()) {
                if(!hp.containsKey(m)) {
                    System.out.println("not in result " + m);
                }
            }

            assertEquals("Should be the same size",wordList.size(),wordcount);
        }
    }
}