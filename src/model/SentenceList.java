package model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * List of Sentences
 *
 * @author Lukas Fischer
 * @author Martin Stoffers
 */
public class SentenceList extends ArrayList<Sentence> {
    private static final long serialVersionUID = -398289415624206352L;

    /**
     * Instantiate a new SentenceList
     *
     * @param set Contains the result with all sentences from database
     * @param opt properties object
     * @throws SQLException Possibly thrown, in {@link SentenceList#importResultSet}
     */
    public SentenceList(ResultSet set, Options opt) throws SQLException {
        importResultSet(set, opt);
    }

    /**
     * Instantiate a new SentenceList with an initial size
     *
     * @param size sets the ArrayList to an initial size
     */
    public SentenceList(int size) {
        super(size);
    }

    /**
     * @param set Contains the result with all sentences from database
     * @param opt properties object
     * @throws SQLException Thrown, if ResultSet has an error
     */
    private void importResultSet(ResultSet set, Options opt) throws SQLException {
        while (set.next()) {
            int sid = set.getInt("s_id");
            String sentence = set.getString("sentence");
            Sentence s = new Sentence(sid, sentence, opt);
            this.add(s);
        }
    }
}
