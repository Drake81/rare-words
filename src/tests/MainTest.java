package tests;

import model.Options;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sql.SqlObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Properties;

public class MainTest {

    private SqlObject db_test = null;
    private Options opt = null;

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

    }

    @After
    public void tearDown() throws Exception {
        db_test.close();
    }


    @Test
    public void testMain() throws Exception {

    }
}