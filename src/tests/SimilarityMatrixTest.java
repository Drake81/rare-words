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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SimilarityMatrixTest {

    private SqlObject db_test = null;
    private Options opt = null;

    private String server = null;
    private int port = 3306;
    private String user = null;
    private String pass = null;
    private String testdb = null;

    private Properties properties;
    private SentenceList sentenceList;


    @Before
    public void setUp() throws Exception {
        FileInputStream file =  new FileInputStream("credentials.properties");
        BufferedInputStream stream = new BufferedInputStream(file);
        this.properties = new Properties();
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
    public void testCalculateSimilarity() throws Exception {
        Options opt = new Options(properties);

        String sentenceString = "abcdef 1245567890987654 testtest123 aaaaaaaaaa.";
        Sentence s2 = new Sentence(1, sentenceString, opt);
        Sentence s1 = new Sentence(2, sentenceString, opt);
        sentenceString += " dddddddddddddddd!!!!";
        Sentence s3 = new Sentence(3, sentenceString, opt);

        SentenceList sList = new SentenceList(3);
        sList.addAll(Arrays.asList(s1,s2,s3));

        SimilarityMatrix matrix = new SimilarityMatrix(sList, opt);
        matrix.calculateSimilarity();
        List<SimilarityEntry> m = matrix.getSimilarityMatrix();
        for(SimilarityEntry s: m){
            if(s.sentenceId_1 == 1 && s.sentenceId_2 == 2){
                assertEquals("Wrong similarity", 1.0f, s.similarity.floatValue(), 0.01f);
            } else if(s.sentenceId_1 == 1 && s.sentenceId_2 == 3){
                assertNotEquals("Wrong similarity", 1.0f, s.similarity.floatValue(), 0.01f);
            }
        }
    }

    @Test
    public void testSimilarityMatrix() throws Exception {

        final int MAX_COUNT = 20;
        int similaritycount = 0;

        Options opt = new Options(properties);

        SentenceList slist = db_test.getSentences(MAX_COUNT);
        SimilarityMatrix matrix = new SimilarityMatrix(slist, opt);
        matrix.calculateSimilarity();

        System.out.println(matrix);

        db_test.cleanTable("sentence_similarity");
        db_test.insertSentenceSimilarities(matrix);


        List<SimilarityEntry> testmatrix = matrix.getSimilarityMatrix();

        if(!db_test.isTableEmpty("sentence_similarity")){
            ResultSet resultset = db_test.executeQuery("SELECT * FROM sentence_similarity");
            while (resultset.next()) {
                similaritycount++;
            }
            assertEquals("Should be the same size",testmatrix.size(),similaritycount);
        }
    }
}
