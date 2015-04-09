package tests;

import model.Options;
import model.Sentence;
import model.SentenceList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import similarity.SimilarityEntry;
import similarity.SimilarityMatrix;
import sql.SqlObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SqlObjectTest {

    private SqlObject db_test = null;

    private String server = null;
    private int port = 3306;
    private String user = null;
    private String pass = null;
    private String testdb = null;
    private Properties properties;

    @Before
    public void setUp() throws Exception {
        FileInputStream file =  new FileInputStream("credentials.properties");
        BufferedInputStream stream = new BufferedInputStream(file);
        this.properties = new Properties();
        properties.load(stream);
        stream.close();
        Options opt = new Options(properties);

        server = properties.getProperty("server");
        port = Integer.parseInt(properties.getProperty("port"));
        user = properties.getProperty("user");
        pass = properties.getProperty("pass");
        testdb = properties.getProperty("testdb_unit_tests");

        db_test = new SqlObject(server, port, user, pass, testdb, opt);

    }

    @After
    public void tearDown() throws Exception {
        db_test.close();
    }

    @Test
    public void testSetNewServer() throws Exception  {
        try {
            db_test.setNewServer("fastreboot.de",1337);
            fail("should not reach this");
        } catch (SQLException e) { }

        try {
            db_test.setNewServer("fastreboot.de",66535);
            fail("should not reach this");
        } catch (SQLException e) { }

        try {
            db_test.setNewServer("fastreboot.de",0);
            fail("should not reach this");
        } catch (SQLException e) { }

        try {
            db_test.setNewServer("fastreboot.de",-1);
            fail("should not reach this");
        } catch (SQLException e) { }

        try {
            db_test.setNewServer("example",1337);
            fail("should not reach this");
        } catch (SQLException e) { }

        try {
            db_test.setNewServer("example");
            fail("should not reach this");
        } catch (SQLException e) { }

        try {
            db_test.setNewServer("");
            fail("should not reach this");
        } catch (SQLException e) { }

        db_test.setNewServer(server,port);
    }

    @Test
    public void testSetNewCredentials() throws Exception {
        try {
            db_test.setNewCredentials("foo","bar");
            fail("should not reach this");
        } catch (SQLException e) { }

        try {
            db_test.setNewCredentials(user,"bar");
            fail("should not reach this");
        } catch (SQLException e) { }

        try {
            db_test.setNewCredentials("foo",pass);
            fail("should not reach this");
        } catch (SQLException e) { }

        db_test.setNewCredentials(user,pass);
    }

    @Test
    public void testSetNewDatabase() throws Exception {
        try {
            db_test.setNewDatabase("NotExistingDatabase");
            fail("should not reach this");
        } catch (SQLException e) { }

        db_test.setNewDatabase(testdb);
    }

    @Test
    public void testExecuteQuery() throws Exception {
        try {
            db_test.executeQuery("SELECT count(*) FROM NotATable");
            fail("should not reach this");
        } catch (SQLException e) { }

        ResultSet t1 = db_test.executeQuery("SELECT count(*) FROM sentences");
        t1.next();
        assertEquals("Wrong sentence count.. ", 2000, Integer.parseInt(t1.getString("count(*)")));
    }

    @Test
    public void testGetSentencesByLimit() throws Exception {
        try {
            db_test.getSentences(-10);
            fail("should not reach this");
        } catch (SQLException e) { }
        try {
            db_test.getSentences(0);
            fail("should not reach this");
        } catch (SQLException e) { }

        SentenceList sliste = db_test.getSentences(10);
        assertEquals("Wrong limit result ", 10, sliste.size());
    }

    @Test
    public void testGetSentences() throws Exception {
        SentenceList slist = db_test.getSentences();
        assertEquals("Wrong limit result ", 2000, slist.size());
    }

    @Test
    public void testGetSentenceById() throws Exception {
        String sentence = db_test.getSentenceById(25);
        System.out.println(sentence);
        assertEquals("Wrong Sentence ", "Wefer leitet das Zentrum für marine Umweltforschung (Marum) an der Universität in Bremen.", sentence);
    }

    @Test
    public void testIsTableEmpty() throws Exception {
        try {
            db_test.isTableEmpty("NotATable");
            fail("should not reach this");
        } catch (SQLException e) { }
        try {
            if(db_test.isTableEmpty("sentences")){
                fail("Table sentences should't not be empty");
            }
        } catch (SQLException e) {
            fail("Method failed");
        }
    }

    @Test
    public void testCleanTable() throws Exception {
        // TODO Have a look at the values and table names. Needs tobe tested
        long values[] = {42L,23L,5L};
        HashMap<String, Long> wordlist = new HashMap<String, Long>();
        wordlist.put("fooo-clean", values[0]);
        wordlist.put("bar-clean", values[1]);
        wordlist.put("hello-clean", values[2]);
        String statement1 = "INSERT INTO word_frequency (word, frequency) VALUES ('fooo-clean','42.0') ON DUPLICATE KEY UPDATE frequency=frequency+VALUES(frequency)";
        String statement2 = "INSERT INTO word_frequency (word, frequency) VALUES ('bar-clean','23.0') ON DUPLICATE KEY UPDATE frequency=frequency+VALUES(frequency)";
        String statement3 = "INSERT INTO word_frequency (word, frequency) VALUES ('hello-clean','5.0') ON DUPLICATE KEY UPDATE frequency=frequency+VALUES(frequency)";

        db_test.commit(statement1);
        db_test.commit(statement2);
        db_test.commit(statement3);

        db_test.cleanTable("word_frequency");
        ResultSet result = db_test.executeQuery("SELECT count(*) FROM word_frequency");
        result.next();
        if(result.getInt("count(*)") > 0){
            fail("Table word_frequency not cleaned");
        }
    }

    @Test
    public void testInsertWordCount() throws Exception {
        db_test.cleanTable("word_frequency");
        //build up reference values
        long values[] = {42L,23L,5L};
        HashMap<String, Long> entries = new HashMap<String, Long>();
        entries.put("bar", values[0]);
        entries.put("fooo", values[1]);
        entries.put("hello", values[2]);
        try {
            db_test.insertWordCount(entries);
        } catch (SQLException e) {
            fail("Method failed" + e);
        }
        if (db_test.isTableEmpty("word_frequency")){
            fail("Table is empty. Should have three entries");
        }
        ResultSet result = db_test.executeQuery("SELECT * FROM word_frequency");
        int i = 0;
        while(result.next()){
            assertEquals("Unexpected result", values[i++], result.getLong("frequency"));
        }
        db_test.cleanTable("word_frequency");

    }

    @Test
    public void testInsertSentenceSimilarities() throws Exception {
        db_test.cleanTable("sentence_similarity");
        //build up reference values

        Options opt = new Options(properties);

        SentenceList sentenceList = db_test.getSentences(5);
        SimilarityMatrix similarityMatrix = new SimilarityMatrix(sentenceList, opt);
        similarityMatrix.calculateSimilarity();

        db_test.insertSentenceSimilarities(similarityMatrix);

        if (db_test.isTableEmpty("sentence_similarity")){
            fail("Table is empty. Should have three entries");
        }

        List<SimilarityEntry> matrix = similarityMatrix.getSimilarityMatrix();
        ResultSet result = db_test.executeQuery("SELECT * FROM sentence_similarity");

        while(result.next()){
            int s_id1 = result.getInt("s_id_1");
            int s_id2 = result.getInt("s_id_2");

            //assertEquals("Unexpected result", matrix, result.getFloat("similarity_count"), 0.000001);
        }
        db_test.cleanTable("sentence_similarity");

    }
}
