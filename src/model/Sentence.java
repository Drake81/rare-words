package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Describes a Sentence
 *
 * @author Lukas Fischer
 * @author Martin Stoffers
 */
public class Sentence extends ArrayList<String> {

    private static final long serialVersionUID = 1653429961099134376L;

    /**
     * Contains the ID of the sentence.
     */
    private int sentenceId = -1;

    /**
     * Original length of the sentence
     */
    private int initiallength = -1;

    /**
     * Contains the properties object
     */
    private Options opt = null;

    /**
     * Contains a set of non word characters to be deleted in a sentence
     */
    private static final String[] punctuation_marks = {"\"", "?", "!", ",", ";", "(", ")", "[", "]", "{", "}", "->", "%", "&", "+"};

    /**
     * Instantiate a new Sentence from String
     *
     * @param sid Sentence ID
     * @param sentence Sentence given as string
     * @param opt properties object
     */
    public Sentence(int sid, String sentence, Options opt) {
        sentenceId = sid;
        this.opt = opt;
        this.calculateWords(sentence);
        this.initiallength = 0;
        for (String word: this){
            this.initiallength += word.length();
        }
    }

    /**
     * Instantiate a new Sentence from a collection of words
     *
     * @param sid Sentence ID
     * @param initiallength Length of the Sentence on instantiate
     * @param wordList List of words in the sentence
     */
     public Sentence(int sid, int initiallength ,List<String> wordList) {
         super(wordList.size());
         this.sentenceId = sid;
         this.initiallength = initiallength;
         this.addAll(wordList);
    }

    /**
     * Applies all possible options, like toLowerCase and editUmlauts to the sentence.
     * Afterwards the sentence will be spliced into words.
     * All words will be added to a global HashMap with its amount of occurrence
     *
     * @param sentence A whole sentence from a ResultSet
     */
    private void calculateWords(String sentence) {
        //remove punctuation mark and other character

        for (String replacement: punctuation_marks) {
            sentence = sentence.replace(replacement, "");
        }

        if (opt.isIgnoreCases()) {
            sentence = sentence.toLowerCase();
        }
        if (opt.isEditUmlauts()) {
            sentence = editUmlauts(sentence);
        }

        sentence = sentence.replace("  ", " ");
        this.addAll(Arrays.asList(sentence.split(" ")));

        // Cleanup Sentence by different rules
        // Could be improved as well
        for(int i = 0; i < this.size(); i++) {
            String word = this.get(i);

            if(word.matches("-\\w*")) {
                //System.out.println("- match: " + word);
                this.set(i, word.replace("-",""));
            }
            if(word.matches(":\\w*")) {
                //System.out.println("- match: " + word);
                this.set(i, word.replace(":",""));
            }
            if(word.matches("\\w*:")) {
                //System.out.println("- match: " + word);
                this.set(i, word.replace(":",""));
            }
            if(word.matches("[A-Za-z-]+\\.")) {
                //System.out.print(". match: " + word);
                this.set(i, word.replace(".",""));
                //System.out.println("  --> " + word);
            }
            if(word.matches("\\d{3,10}+\\.")) {
                //System.out.print(". match: " + word);
                this.set(i, word.replace(".",""));
                //System.out.println("  --> " + word);
            }

            if(word.matches("\\d\\d:\\d\\d")) {
                this.remove(i);
                continue;
            }

            if(word.matches(" ")) {
                this.remove(i);
                continue;
            }

            if(word.matches("")) {
                this.remove(i);
                continue;
            }

            if(word.length() < 2) {
                this.remove(i);
            }
        }

    }

    /**
     * Replaces all umlauts with its ascii representation and returns the word
     *
     * @param sentence A word
     * @return The altered word
     */
    private String editUmlauts(String sentence) {
        sentence = sentence.replace("Ä", "Ae");
        sentence = sentence.replace("Ö", "Oe");
        sentence = sentence.replace("Ü", "Ue");
        sentence = sentence.replace("ä", "ae");
        sentence = sentence.replace("ö", "oe");
        sentence = sentence.replace("ü", "ue");
        sentence = sentence.replace("ß", "ss");

        return sentence;
    }

    /**
     * Returns the Sentence ID
     *
     * @return Sentence ID
     */
    public int getId() {
        return sentenceId;
    }


    /**
     * @return Returns the original length of a sentence
     */
    public int getInitiallength() {
        return initiallength;
    }

    /**
     * Returns a string representation of the sentence
     *
     * @return Sentence
     */
    public String toString() {
        String result = "";
        for(String word: this) {
            result += word + " ";
        }
        return result;
    }
}
