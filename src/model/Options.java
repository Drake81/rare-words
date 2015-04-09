package model;

import sql.SqlObject;

import java.util.Properties;

/**
 * Includes all substantial properties fo computation
 *
 * @author Martin Stoffers
 */
public class Options {

    /**
     * Threshold for {@link similarity.SimilarityMatrix}
     */
    private double similarityThreshold = -1.0;

    /**
     * Higher limit for {@link computations.ReduceWordlist#absoluteValues(sql.SqlObject, long, long)}
     */
    private int higherlimit = -1;

    /**
     * Lower limit for {@link computations.ReduceWordlist#absoluteValues(sql.SqlObject, long, long)}
     */
    private int lowerlimit = -1;

    /**
     * Percent value for {@link computations.ReduceWordlist#confidenceFunction(SqlObject, double)}
     */
    private double percentvalue = -1;

    /**
     * Whether umlauts should be applied or not
     */
    private boolean editUmlauts;

    /**
     * Whether ingnoreCases should be applied or not
     */
    private boolean ignoreCases;

    /**
     * Whether only nouns should be counted
     */
    private boolean onlyNouns;

    /**
     * Whether {@link computations.ReduceWordlist#confidenceFunction(sql.SqlObject, double)} should be executed
     */
    private boolean percent;

    /**
     * Whether {@link computations.ReduceWordlist#absoluteValues(sql.SqlObject, long, long)} should be executed
     */
    private boolean byrange;

    /**
     * Whether {@link computations.WordCounter} should be executed
     */
    private final boolean updateWordCounts;

    /**
     * Whether {@link similarity.SimilarityMatrix} should be recalculated
     */
    private boolean updateSimilarities;

    /**
     * Whether the reduce method is used
     */
    private boolean reduce = true;

    /**
     * Minimum MatchLength
     */
    private int minimumMatchlength = 0;

    /**
     * @param properties Properties Object with initial values
     * @throws Exception Thrown, if values are not correct
     */
    public Options(Properties properties) throws Exception {

        this.updateWordCounts = Boolean.parseBoolean(properties.getProperty("updateWordCounts"));

        String tmp = properties.getProperty("filter");
        if (tmp.equals("ignoreCases")) {
            this.ignoreCases = true;
            this.onlyNouns = false;
        } else if (tmp.equals("onlyNouns")) {
            this.ignoreCases = false;
            this.onlyNouns = true;
        } else if (tmp.equals("off")) {
            this.ignoreCases = false;
            this.onlyNouns = false;
        } else {
            throw new Exception("Invalid value for option \"filter\"");
        }

        this.editUmlauts = Boolean.parseBoolean(properties.getProperty("editUmlauts"));

        tmp = properties.getProperty("reduceMethod");
        if (tmp.equals("percent")) {
            this.percent = true;
            this.byrange = false;
            try {
                this.percentvalue = Double.parseDouble(properties.getProperty("percent"));
            } catch (NumberFormatException e) {
                throw new Exception("Invalid value for option \"percent\"");
            }
        } else if (tmp.equals("frequency")) {
            this.percent = false;
            this.byrange = true;
            try {
                this.lowerlimit = Integer.parseInt(properties.getProperty("lowerfreq"));
                this.higherlimit = Integer.parseInt(properties.getProperty("higherfreq"));
            } catch (NumberFormatException e) {
                throw new Exception("Invalid value for option \"lowerfreq\" or \"higherfreq\"");
            }
        } else if (tmp.equals("off")) {
            this.percent = false;
            this.byrange = false;
            this.reduce = false;
        }
        else {
            throw new Exception("Invalid value for option \"reduceMethod\"");
        }

        try {
            this.minimumMatchlength = Integer.parseInt(properties.getProperty("minimumMatchLength"));
        } catch (NumberFormatException e) {
            throw new Exception("Invalid value for option \"minimumMatchLength\"");
        }

        this.updateSimilarities = Boolean.parseBoolean(properties.getProperty("updateSimilarities"));
        try {
            this.similarityThreshold = Double.parseDouble(properties.getProperty("similarityThreshold"));
        }
        catch(NumberFormatException e) {
            throw new Exception("Invalid value for option \"similarityThreshold\"");
        }
    }

    /**
     * @return Returns true if umlauts should be rewritten
     */
    public boolean isEditUmlauts() {
        return editUmlauts;
    }

    /**
     * @return Returns true if all sentences should be in lowercase
     */
    public boolean isIgnoreCases() {
        return ignoreCases;
    }

    /**
     * @return Returns true if {@link computations.WordCounter} should only count Nouns
     */
    public boolean isOnlyNouns() {
        return onlyNouns;
    }

    /**
     * @return Returns true if the word_frequency table should be recalculated
     */
    public boolean isUpdateWordCounts() {
        return updateWordCounts;
    }

    /**
     * @return Returns true if the sentence_similarity table should be recalculated
     */
    public boolean isUpdateSimilarities() {
        return updateSimilarities;
    }

    /**
     * @return Returns true if reduce should be computed with {@link computations.ReduceWordlist#absoluteValues(sql.SqlObject, long, long)}
     */
    public boolean isReduce() {
        return reduce;
    }

    /**
     * @return Returns true if reduce should be computed with {@link computations.ReduceWordlist#absoluteValues(sql.SqlObject, long, long)}
     */
    public boolean isPercent() {
        return percent;
    }

    /**
     * @return Returns true if reduce should be computed with {@link computations.ReduceWordlist#confidenceFunction(sql.SqlObject, double)} )}
     */
    public boolean isByRange() {
        return byrange;
    }

    /**
     * @return Returns the percent value for {@link computations.ReduceWordlist#confidenceFunction(sql.SqlObject, double)}
     */
    public double getPercentValue() {
        return percentvalue;
    }

    /**
     * @return Returns the higher limit for {@link computations.ReduceWordlist#absoluteValues(sql.SqlObject, long, long)}
     */
    public int getLowerLimit() {
        return lowerlimit;
    }

    /**
     * @return Returns the lower limit for {@link computations.ReduceWordlist#absoluteValues(sql.SqlObject, long, long)}
     */
    public int getHigherLimit(){
        return higherlimit;
    }

    /**
     * @return Returns the threshold for {@link similarity.SimilarityMatrix}
     */
    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    /**
     * @return Returns the threshold for {@link model.SentenceList}
     */
    public int getMinimumMatchLength() {
        return minimumMatchlength;
    }
}
