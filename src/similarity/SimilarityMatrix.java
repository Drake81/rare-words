package similarity;

import model.Options;
import model.Sentence;
import model.SentenceList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Similarity matrix
 *
 * @author Lukas Fischer
 * @author Martin Stoffers
 */
public class SimilarityMatrix {
    /**
     *
     * Logger object
	 */
	private static final Logger log = Logger.getLogger(SimilarityMatrix.class.getName());

    /**
     * Contains options for the calculation
     */
    private final Options opt;

    /**
     * Contains the {@link model.SentenceList} object given by constructor
     */
    private SentenceList sentenceList;

    /**
     * Contains the size of the {@link #sentenceList}
     */
    private int size;

    /**
     * Stores the state of the calculation
     */
    private boolean calculationFinished = false;

    /**
     * Contains the resulting similarity matrix
     */
    private List<SimilarityEntry> matrix;

    /**
     * Counts similarities with values below threshold
     */
    private int zeroSimilarityCount = 0;


    /**
     * Instantiate a new similarities matrix with a list of sentences
     *
     * @param slist Contains a the {@link model.SentenceList}
     * @param opt Options, which should be applied
     */
    public SimilarityMatrix(SentenceList slist, Options opt) {
        this.opt = opt;
        this.sentenceList = slist;
        size = slist.size();
        matrix = new ArrayList<SimilarityEntry>(size);
    }

    /**
     * Instantiate a new similarities matrix with a list of sentences
     * and a set of rare words to be used in the calculation
     *
     * @param sentenceList Contains a the {@link model.SentenceList}
     * @param rareWords contains words that will be considered when comparing sentences
     * @param opt Options, which should be applied
     */
    public SimilarityMatrix(SentenceList sentenceList, HashMap<String,Double> rareWords, Options opt) {
        this.opt = opt;
        this.sentenceList = new SentenceList(sentenceList.size());
        for(Sentence sentence: sentenceList){
            ArrayList<String> goodWords = new ArrayList<String>(sentence.size());
            for(String word: sentence){
                if( rareWords.containsKey(word)){
                    goodWords.add(word);
                }
            }
            if(goodWords.size() >= opt.getMinimumMatchLength()) {
                this.sentenceList.add(new Sentence(sentence.getId(), sentence.getInitiallength(), goodWords));
            }
        }
        this.size = this.sentenceList.size();
        matrix = new ArrayList<SimilarityEntry>(size);
    }

    /**
     * Computes all similarities from the given {@link #sentenceList}
     * Must not be called more than once.
     */
    public void calculateSimilarity() {
        double threshold = this.opt.getSimilarityThreshold();

        final double percent = 0.05;

        long stepcount=0;
        int outputpercent=0;
        long calculatedstep = (long) (((long) this.size * (((long) this.size )-1)/2) * percent);
        if (calculatedstep < 1) {
            calculatedstep = 1;
        }
        System.out.println("Calculate " + ((long) this.size * (((long) this.size )-1))/2 + " similarities between " + this.size + " Sentences");

        System.out.println("\n");
        for (int i = 0; i < size; ++i) {
            for (int j = i+1; j < size; ++j) {
                float similarity = 0.0f;
                stepcount++;
                if(stepcount % calculatedstep == 1){
                    System.out.println(outputpercent + "% calculated");
                    outputpercent += (int)(percent*100);
                    stepcount=1;
                }

                if( sentenceList.get(i).size() > 0 && sentenceList.get(j).size() > 0 ) {
                    similarity = calculateSimilarity(sentenceList.get(i), sentenceList.get(j));
                }
                if(similarity > threshold){
                    matrix.add(new SimilarityEntry(sentenceList.get(i).getId(), sentenceList.get(j).getId(), similarity));
                } else {
                    zeroSimilarityCount += 1;
                }
            }
        }
        this.calculationFinished = true;
    }

    /**
     * Computes a simple similarity between two sentences.
     * The similarity is the quotient of the length from both sentences
     *
     * @param s1 Sentence One
     * @param s2 Sentence Two
     * @return Similarity between the given sentences
     */
    private float calculateSimilarity(Sentence s1, Sentence s2) {
        float sim = 0.0F;
        int s1_length = s1.getInitiallength();
        int s2_length = s2.getInitiallength();
        int s1_words  = s1.size();
        int s2_words  = s2.size();
        HashMap<String,Integer> foundwords = new HashMap<String,Integer>();

        float avglength = 0.5f * (s1_length + s2_length);
        float avgwordcount = 0.5f * (s1_words + s2_words);

        int match_length = 0;
        int matched_wordcount = 0;

        s1_loop: for(String wordS1: s1){
            for(String wordS2: s2){
                if(wordS1.equals(wordS2)) {
                    if (!foundwords.containsKey(wordS1)) {
                        foundwords.put(wordS1, wordS1.length());
                    }
                    continue s1_loop;
                }
            }
        }

        matched_wordcount = foundwords.size();
        for (String word : foundwords.keySet()) {
            match_length += word.length();
        }

        sim = 0.5f*(((float)matched_wordcount/avgwordcount) + ((float)match_length / avglength));

        /*if(sim > opt.getSimilarityThreshold()) {
            log.info("\t" + matched_wordcount + " / " +
                     "(" + s1_words + ">avg<" + s2_words + ")-> " + avgwordcount +
                     "   +   " + match_length + " / " +
                     "(" + s1_length + ">avg<" + s2_length + ")-> " + avglength +
                     "\t\t = " + sim +
                     "\t\t(" + s1.getId() + "|" + s2.getId() + ")");
        }*/
        return sim;
    }

    /**
     * Calculates the normalized similarity distribution of this matrix
     *
     * @param granularity the number of data points to be calculated for this distribution
     * @return List of normalized frequencies
     */
    public List<Double> similarityDistribution(int granularity){
        int counts[] = new int[granularity];
        int count_all = this.zeroSimilarityCount;
        counts[0] = this.zeroSimilarityCount;
        for(SimilarityEntry s: this.matrix){
            int position = (int)(s.similarity*granularity);
            position = (position == granularity)?(position - 1):position;
            counts[position] += 1;
            count_all += 1;
        }
        ArrayList<Double> dist = new ArrayList<Double>(granularity);
        for(int position=0; position<granularity; position++){
            Double value = counts[position] / (double) count_all;
            dist.add(position, value);
        }
        return dist;
    }

    /**
     * Prints the resulting similarity matrix in a human readable format.
     *
     * @return Transformed matrix as String
     */
    public String toString() {
        final int MAX_ENTRIES = 10;

        if( !this.calculationFinished ){
            return "There is no data!";
        }

        String result = "";

        result += "Distribution: "+this.similarityDistribution(MAX_ENTRIES) + "\n";
        result += "Similarities:\n";

        int entry_count = 0;
        for(SimilarityEntry s: matrix){
            result += "("+s.sentenceId_1+"|"+s.sentenceId_2+") : " + s.similarity;
            entry_count += 1;
            if( entry_count == MAX_ENTRIES ){
                result += "\n(.....)";
                break;
            }
            result += "\n";
        }
        return result;
    }

    /**
     * @return SimilarityMatrix
     */
    public  List<SimilarityEntry> getSimilarityMatrix() {
        return matrix;
    }
}
