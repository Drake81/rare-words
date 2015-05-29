/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package computations;

import model.Options;
import model.Sentence;
import model.SentenceList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * Counts amount of all words in table sentences in a database.
 * Works on on a given ResultSet by {@link sql.SqlObject}
 *
 * @author Karsten Brandt
 * @author Martin Stoffers
 */
public class WordCounter {

    /**
     * Logger object
     */
    private static final Logger log = Logger.getLogger(WordCounter.class.getName());

    /**
     * Properties Options
     */
    private final Options opt;


    /**
     * Contains the SentenceList given to the constructor
     */
    private SentenceList sentenceList = null;

    /**
     * Contains the resulting wordList
     */
    private final HashMap<String, Long> wordList = new HashMap<String, Long>();
    
    /**
     * Variable for statistics
     */
    private long wordCount = 0;

    /**
     * Builds a hashmap indexed by all words there overall count
     *
     * @param sentenceList Contains a set of sentences returned by {@link sql.SqlObject#getSentences}.
     * @param opt Properties Options
     */
    public WordCounter(SentenceList sentenceList, Options opt) {
        this.sentenceList = sentenceList;
        this.opt = opt;
        start();
    }

    /**
     * Starts the the wordcounter on each sentence from ResultSet
     * <p>
     * Will be the run method in the future threaded versions
     */
    private void start(){
        System.out.println("Calculate word list");
        for (Sentence s: sentenceList) {
            countWords(s);
        }

        int i = 0;
        Iterator<Entry<String, Long>> iter = wordList.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Long> entry = iter.next();
            if(entry.getValue() == 1L){
                iter.remove();
                wordCount--;
                i++;
            }
        }

        System.out.println(i + " words with only a single occurrence removed.");
        System.out.println("WordCount finished with " + sentenceList.size() + " sentences processed and " + wordCount + " words found");
    }

    /**
     * Applies all possible options, like toLowerCase and editUmlauts to the sentence.
     * Afterwards the sentence will be spliced into words.
     * All words will be added to a global HashMap with its amount of occurrence
     *
     * @param sentence A whole sentence from a ResultSet
     */
    private void countWords(Sentence sentence) {

        for (int i = 1; i < sentence.size(); i++) {
            String word = sentence.get(i);

            // If Word longer then 70 chars - skip word
            if(word.length() > 69) {
                continue;
            }

            if(opt.isOnlyNouns()) {
                try {
                    // TODO More fixing...
                    if(!(Character.isUpperCase(word.codePointAt(0)))) {
                        continue;
                    }
                }
                catch(StringIndexOutOfBoundsException e){
                    //System.out.println("Fehler in Wort >" + sentence.get(i) +"<");
                    //System.out.println("Wortlänge " + sentence.get(i).length());
                    //System.out.println("Wörter im Satz " + sentence.size());
                    //System.out.println("Satz-Id: " + sentence.getId());
                    //System.out.println("Satz: " + sentence.toString());
                    //System.out.println();
                }
            }

            if (wordList.containsKey(word)) {
                long count = wordList.get(word);
                wordList.put(word, ++count );
            }
            else {
                wordList.put(word, 1L);
                //log.log(Level.INFO, "Word \"{0}\" added", sentence.get(i));
                wordCount++;
            }
        }
    }

    /**
     * Gets the HashMap with all words and counts
     *
     * @return Hashmap with words an counts
     */
    public HashMap<String, Long> getWordCounts() {
        return wordList;
    }
}
