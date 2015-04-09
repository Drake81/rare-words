package similarity;

/**
 * Describes one Entry in {@link similarity.SimilarityMatrix}
 *
 * @author Lukas Fischer
 * @author Martin Stoffers
 */
public class SimilarityEntry {
    public final Integer sentenceId_1;
    public final Integer sentenceId_2;
    public final Float similarity;

    /**
     * @param s_id1 Sentence ID
     * @param s_id2 Sentence ID
     * @param similarity value, which indicates the similarity between both sentences
     */
    public SimilarityEntry(int s_id1, int s_id2, float similarity){
        sentenceId_1 = s_id1;
        sentenceId_2 = s_id2;
        this.similarity = similarity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof SimilarityEntry))
            return false;
        SimilarityEntry similarityEntryo = (SimilarityEntry) o;
        return this.sentenceId_1.equals(similarityEntryo.sentenceId_1) &&
               this.sentenceId_2.equals(similarityEntryo.sentenceId_2);
    }

    /**
     * Returns the calculated hash of an SimilarityEntry
     *
     * @return Hash of object
     */
    public String getHash(){
        return (sentenceId_1 + ":" + sentenceId_2);
    }
}
