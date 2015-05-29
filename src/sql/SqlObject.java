package sql;

import computations.WordCounter;
import model.Options;
import model.SentenceList;
import similarity.SimilarityEntry;
import similarity.SimilarityMatrix;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages all querys and connections to the databases
 *
 * @author Karsten Brandt
 * @author Martin Stoffers
 */
public class SqlObject {

    /**
     * Logger object from {@link java.util.logging.Logger}
     */
    private static final Logger log = Logger.getLogger(WordCounter.class.getName());

    /**
     * Properties object
     */
    private Options opt = null;

    /**
     * Servername (default to <b>localhost</b>)
     */
    private String server = "localhost";

    /**
     * Username
     */
    private String user = null;

    /**
     * Password
     */
    private String password = null;

    /**
     * Port (default to 3306)
     */
    private int port = 3306;

    /**
     * Databasename
     */
    private String database = null;

    /**
     * Contains the default connect object from {@link java.sql.Connection}
     */
    private Connection connect = null;

    /**
     * Connects to Database with the given parameters from constructors
     * Needs "com.mysql.jdbc.Driver" to work
     *
     * @throws SQLException Thrown, if driver is not found or database connection failed
     */
    private void connect() throws SQLException{
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            log.severe("MySQL Driver not found");
            throw new SQLException(e);
        }
        try {
            connect = DriverManager.getConnection("jdbc:mysql://" + server + ":" + port + "/"+ database + "?autoReconnect=true" + "&user=" + user + "&password=" + password + "");
        } catch (SQLException e) {
            log.severe("MySQL Connect Error:\n" + e.toString());
            throw new SQLException(e);
        }
    }

    /**
     * Reconnects to Database with the given parameters
     *
     * @throws SQLException Thrown, if driver is not found or database connection failed
     */
    private void reconnect() throws SQLException{
        this.connect.close();
        this.connect();
    }

    /**
     * Close connection to to Database
     *
     * @throws SQLException Thrown, if driver is not found or database connection failed
     */
    public void close() throws SQLException{
        this.connect.close();
    }

    /**
     * Instantiate a Database object with the given parameters.
     * If {@link #database} is empty the connection in {@link sql.SqlObject#connect} is made without a table select.
     *
     * @param server Server which should be used (example.org)
     * @param port Port which should be used
     * @param user Username for authentication
     * @param password Password for authentication
     * @param database Database which should be used in every query
     * @param opt Options, which should be applied
     * @throws SQLException Thrown, if driver is not found or database connection failed
     */
    public SqlObject(String server, int port, String user, String password, String database, Options opt) throws SQLException{
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
        this.database = database;
        this.opt = opt;
        if(this.port < 1 || this.port > 65535){
            throw new SQLException("Port not in valid range");
        }
        connect();
    }

    /**
     * Instantiate a Database object with the given parameters.
     * If {@link #database} is empty the connection in {@link sql.SqlObject#connect} is made without a table select.
     * Port will be set to standard SQL port
     *
     * @param server Server which should be used (example.org)
     * @param user Username for authentication
     * @param password Password for authentication
     * @param database Database which should be used in every query
     * @param opt Options, which should be applied
     * @throws SQLException Thrown, if driver is not found or database connection failed
     */
    public SqlObject(String server, String user, String password, String database, Options opt) throws SQLException{
        this.server = server;
        this.user = user;
        this.password = password;
        this.database = database;
        this.opt = opt;
        this.connect();
    }

    /**
     * Instantiate a Database object with the given parameters.
     * If {@link #database} is empty the connection in {@link sql.SqlObject#connect} is made without a table select.
     * Port will be set to standard SQL port
     *
     * @param user Username for authentication
     * @param password Password for authentication
     * @param database Database which should be used in every query
     * @param opt Options, which should be applied
     * @throws SQLException Thrown, if driver is not found or database connection failed
     */
    public SqlObject(String user, String password, String database, Options opt) throws SQLException{
        this.user = user;
        this.password = password;
        this.database = database;
        this.opt = opt;
        this.connect();
    }

    /**
     * Sets a new server
     * Afterwards a reconnect is made
     *
     * @param server Server which should be used (example.org)
     * @throws SQLException Thrown, if driver is not found or database connection failed
     */
    public void setNewServer(String server) throws SQLException {
        this.server = server;
        this.reconnect();
    }

    /**
     * Sets a new server and port
     * Afterwards a reconnect is made
     *
     * @param server Server which should be used (example.org)
     * @param port Port which should be used
     * @throws SQLException Thrown, if driver is not found or database connection failed
     */
    public void setNewServer(String server, int port) throws SQLException {
        this.server = server;
        this.port = port;
        if(this.port < 1 || this.port > 65535){
            throw new SQLException("Port not in valid range");
        }
        this.reconnect();
    }

    /**
     * Sets a new user and password
     * Afterwards a reconnect is made
     *
     * @param user Username for authentication
     * @param password Password for authentication
     * @throws SQLException Thrown, if driver is not found or database connection failed
     */
    public void setNewCredentials(String user, String password) throws SQLException {
        this.user = user;
        this.password = password;
        this.reconnect();
    }

    /**
     * Sets a new database
     * Afterwards a reconnect is made
     *
     * @param database Database which should be used (example.org)
     * @throws SQLException Thrown, if driver is not found or database connection failed
     */
    public void setNewDatabase(String database) throws SQLException {
        this.database = database;
        this.reconnect();
    }

    /**
     * Executes a query to given server an if exists to a specific database
     *
     * @param query A String which contains the query
     * @return Contains the result of the given query
     * @throws SQLException Thrown, if query failed
     */
    public ResultSet executeQuery(String query) throws SQLException {
        if(this.connect.isClosed()){
            this.connect();
        }

        Statement statement = connect.createStatement();
        return statement.executeQuery(query);
    }

    /**
     * Get a set of sentences from table <b>sentences</b> up to the given maximum in limit
     *
     * @param limit Limits the query to the given amount
     * @return Contains the returned sentences
     * @throws SQLException Thrown, if query failed
     */
    public SentenceList getSentences(int limit) throws SQLException {
        if(limit < 1) {
            throw new SQLException("Given limit is out of range");
        }
        if(this.connect.isClosed()) {
            this.connect();
        }

        if(limit <= 0){
            throw new SQLException("Limit is negativ or null");
        }

        Statement statement = connect.createStatement();

        String query = "SELECT s_id, sentence FROM sentences limit " + limit;
        ResultSet result =  statement.executeQuery(query);
        System.out.println("Preprocessing Sentences");
        return new SentenceList(result, opt);
    }

    /**
     * Gets the complete set of sentences from table <b>sentences</b>
     * Table must exists in given database
     *
     * @return Contains the returned sentences
     * @throws SQLException Thrown, if query failed
     */
    public SentenceList getSentences() throws SQLException {
        if(this.connect.isClosed()){
            this.connect();
        }

        Statement statement = connect.createStatement();

        String query = "SELECT s_id, sentence FROM sentences";
        ResultSet result =  statement.executeQuery(query);

        System.out.println("Preprocessing Sentences");
        return new SentenceList(result, opt);
    }

    /**
     * Gets the complete set of sentences from table <b>sentences</b>
     * Table must exists in given database
     *
     * @param sentenceId is the Id of the desired sentence
     * @return the sentence
     * @throws SQLException Thrown, if query failed
     */
    public String getSentenceById(int sentenceId) throws SQLException {
        if(this.connect.isClosed()){
            this.connect();
        }

        Statement statement = connect.createStatement();

        String query = "SELECT sentence FROM sentences WHERE s_id = " + sentenceId;
        ResultSet result =  statement.executeQuery(query);
        result.first();
        return result.getString("sentence");
    }

    /**
     * Tests a given table to emptiness
     *
     * @param table The table, which should be tested
     * @return Is true if table is empty, otherwise false will be returned
     * @throws SQLException Thrown, if query failed
     */
    public boolean isTableEmpty(String table) throws SQLException {
        if(table.isEmpty()){
            throw new SQLException("Empty Table String");
        }
        if(this.connect.isClosed()){
            this.connect();
        }

        Statement statement = connect.createStatement();

        String query = "SELECT count(*) FROM " + table + "";
        ResultSet resultSet = statement.executeQuery(query);

        resultSet.next();
        return resultSet.getInt("count(*)") <= 0;
    }

    /**
     * Deletes each entry in the given table
     *
     * @param table The table, which should be cleaned entirely
     * @throws SQLException Thrown, if query failed
     */
    public void cleanTable(String table) throws SQLException {
        if(table.isEmpty()){
            throw new SQLException("Empty Table String");
        }
        if(this.connect.isClosed()){
            this.connect();
        }

        Statement statement = connect.createStatement();
        String query = "DELETE FROM " + table;
        statement.executeUpdate(query);
    }


    /**
     * Create tables for calculation
     *
     * @throws SQLException Thrown if create statements failed
     */
    public void prepare() throws  SQLException {
        if (this.connect.isClosed()) {
            this.connect();
        }

        this.createTable(" CREATE TABLE IF NOT EXISTS word_frequency" +
                " ( word varchar(70) NOT NULL, " +
                " frequency bigint(20) NOT NULL, " +
                " PRIMARY KEY (word));");// +
                //" ENGINE=InnoDB DEFAULT CHARSET=latin1;");

        this.createTable(" CREATE TABLE IF NOT EXISTS sentence_similarity " +
                " (similarity float NOT NULL," +
                " s_id_1 int(10) NOT NULL," +
                " s_id_2 int(10) NOT NULL," +
                " PRIMARY KEY (`s_id_1`,`s_id_2`));");// +
                //" ENGINE=InnoDB DEFAULT CHARSET=latin1;");

    }

    /**
     * Inserts a given word list into the table <b>word_frequency</b>
     * Table must exists in given database
     *
     * @param wordlist Hashmap with words and frequency count
     * @throws SQLException Thrown, if query failed
     */
    public void insertWordCount(HashMap<String, Long> wordlist) throws SQLException {
        //TODO Upper and Lowercase in Database
        if(wordlist.isEmpty()) {
            throw new SQLException("Given WordList is empty. Nothing to commit");
        }
        if(this.connect.isClosed()){
            this.connect();
        }

        this.connect.setAutoCommit(false);
        PreparedStatement preparedStatement = connect.prepareStatement("INSERT INTO word_frequency (word, frequency) VALUES (?,?) ON DUPLICATE KEY UPDATE frequency=VALUES(frequency)");
        for (String word: wordlist.keySet()) {
            preparedStatement.setString(1, word);
            preparedStatement.setLong(2, wordlist.get(word));
            preparedStatement.addBatch();
        }
        if(this.connect.isClosed()){
            this.connect();
        }
        preparedStatement.executeBatch();
        if(this.connect.isClosed()){
            this.connect();
        }
        this.connect.commit();
        this.connect.setAutoCommit(true);
    }

    /**
     * Inserts a new column into table sentence_similarity
     * Table must exists in given database
     *
     * @param similarityMatrix Matrix with similarities returned by {@link similarity.SimilarityMatrix}
     * @throws SQLException Thrown, if query failed
     */
    public void insertSentenceSimilarities(SimilarityMatrix similarityMatrix) throws SQLException {

        List<SimilarityEntry> matrix = similarityMatrix.getSimilarityMatrix();

        if(matrix.size() <= 0) {
           throw new SQLException("The given matrix is invalid");
        }

        if(this.connect.isClosed()){
            this.connect();
        }

        this.connect.setAutoCommit(false);
        PreparedStatement preparedStatement = connect.prepareStatement("INSERT INTO sentence_similarity (s_id_1, s_id_2, similarity) VALUES (?,?,?) ON DUPLICATE KEY UPDATE similarity=values(similarity)");

        for(SimilarityEntry s: matrix){
            preparedStatement.setInt(1, s.sentenceId_1);
            preparedStatement.setInt(2, s.sentenceId_2);
            preparedStatement.setFloat(3, s.similarity);
            preparedStatement.addBatch();
        }
        if(this.connect.isClosed()){
            this.connect();
        }
        preparedStatement.executeBatch();
        this.connect.commit();
        if(this.connect.isClosed()){
            this.connect();
        }
        this.connect.setAutoCommit(true);
    }

    /**
     * Commits a statement to the database
     *
     * @param commitStatement A valid statement like insert or update
     * @throws SQLException Thrown, if query failed
     */
    public void commit(String commitStatement) throws SQLException {
        PreparedStatement preparedStatement = connect.prepareStatement(commitStatement);
        preparedStatement.executeUpdate();
    }

     /**
     * Creates a new table
     *
     * @param createStatement A valid create statement
     * @throws SQLException Thrown, if query failed
     */
    public void createTable(String createStatement) throws SQLException {
        Statement createstmt = connect.createStatement();
        createstmt.executeUpdate(createStatement);
    }
}











