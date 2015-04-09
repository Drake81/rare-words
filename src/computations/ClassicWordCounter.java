package computations;

import model.ObjectCounter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Old Wordcounter class
 *
 * @deprecated Is replaced be {@link computations.WordCounter}
 * @author Karsten Brandt
 * @author Martin Stoffers
 */
public class ClassicWordCounter {

    private static final Logger log = Logger.getLogger(WordCounter.class.getName());
    private static final String[] punctuation_marks = {"\"", ".", "?", "!", ":", ",", ";", "(", ")", "[", "]", "{", "}", "->"};

    private final boolean editUmlauts;
    private final boolean ignoreCases;
    private final ResultSet resultset;

    //ArrayList<ObjectCounter> wordList;
    private final ArrayList<ObjectCounter> wordList = new ArrayList<ObjectCounter>();

    /**
     * @param resultset ResultSet from Database Query
     */
    public ClassicWordCounter(ResultSet resultset) {
        this.resultset=resultset;
        this.editUmlauts = false;
        this.ignoreCases = false;
        start();
    }

    /**
     * Builds a hashmap indexed by all words there overall count
     *
     * @param resultset Contains a set of sentences returned by {@link sql.SqlObject#getSentences}.
     * @param editUmlauts if true, all umlauts will be replaced ((ö,oe),(ä,ae),(ü,ue),(ß,ss)).
     * @param ignoreCases if true, all words will be converted to lowercase.
     */
    public ClassicWordCounter(ResultSet resultset, boolean editUmlauts, boolean ignoreCases) {
        this.resultset=resultset;
        this.editUmlauts = editUmlauts;
        this.ignoreCases = ignoreCases;
        start();
    }

    /**
     * Starts the the wordcounter on each sentence from ResultSet
     */
    private void start(){
        try {
            while (this.resultset.next()) {
                countWords(resultset.getString(2));
            }
        } catch (SQLException e) {
            log.severe(e.toString());
        }
    }

    /**
     * Applies all possible options, like toLowerCase and editUmlauts to the sentence.
     * Afterwards the sentence will be spliced into words.
     * All words will be added to a global HashMap with its amount of occurrence
     *
     * @param sentence A whole sentence from a ResultSet
     */
    private void countWords(String sentence) {
        //remove punctuation mark and other character
        for (String replacement: punctuation_marks) {
            sentence = sentence.replace(replacement, "");
        }
        if (editUmlauts) {
            sentence = editUmlauts(sentence);
        }
        if (ignoreCases) {
            sentence = sentence.toLowerCase();
        }

        String wordArray[] = sentence.split(" ");
        boolean wordNotFound;
        for (String aWordArray : wordArray) {
            wordNotFound = true;
            for (ObjectCounter aWordList : wordList) {
                if (aWordArray.equals(aWordList.name)) {
                    aWordList.count++;
                    wordNotFound = false;
                    break;
                }
            }
            if (wordNotFound) {
                wordList.add(new ObjectCounter(aWordArray));
                //System.out.println("Word " + wordArray[i] + " added");
            }
        }
        //System.out.println("Wordcounter finished with " + wordList.size() + " found elements of " + resultset.);
    }

    /**
     * Replaces all umlauts with its ascii representation and returns the word
     *
     * @param word A word
     * @return The altered word
     */
    private String editUmlauts(String word) {
        word = word.replace("ä", "ae");
        word = word.replace("ö", "oe");
        word = word.replace("ü", "ue");
        word = word.replace("ß", "ss");
        
        return word;
    }
}
