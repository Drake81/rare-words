package computations;

import sql.SqlObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collection of functions which compute a list of rare words and return a
 * HashMap with them included
 * 
 * @author karsten
 */
public class ReduceWordlist {

	/**
	 * Logger object
	 */
	private static final Logger log = Logger.getLogger(WordCounter.class
			.getName());

	private static final HashMap<String, Double> wordList = new HashMap<>();

	/**
	 * AbsoluteValuesFunction returns hashmap with rare words depending on
	 * user-set limits
	 * 
	 * @param sql
	 *            which provides access to existing database
	 * @param lower_frequency
	 *            absolute value as lower limit for word frequency
	 * @param upper_frequency
	 *            absolute value as upper limit for word frequency
	 * @return Hashmap with rare words depending on input parameters
	 * @throws java.sql.SQLException
	 *             Thrown if query fails
	 */
	public static HashMap<String, Double> absoluteValues(SqlObject sql,
			long lower_frequency, long upper_frequency) throws SQLException {
		System.out.println("\tFinding rare words ... with absolute values");

		ResultSet maxValue = sql
				.executeQuery("SELECT max(frequency) FROM word_frequency;");
		long size = 0;
		while (maxValue.next()) {
			size = maxValue.getLong("max(frequency)");
		}

		System.out.println("\tGet word frequencies");
		ResultSet words = sql
				.executeQuery("SELECT word,frequency FROM word_frequency where frequency >= "
						+ lower_frequency
						+ " AND frequency <= "
						+ upper_frequency + ";");
		while (words.next()) {
			double probability = ((double) words.getLong("frequency"))
					/ ((double) size);
			wordList.put(words.getString("word"), probability);
		}
		return wordList;
	}

	/**
	 * Confidence-Function computes automatically lower and upper endpoint for
	 * word_frequency depending on input parameter percent returns complete
	 * hashmap of rare words
	 * 
	 * @param sql
	 *            objects which provides access to existing database system
	 * @param percent
	 *            percentage of whole dataset which will be returned
	 * @return Hashmap with rare words depending on input parameters
	 * @throws SQLException
	 *             Thrown if query fails
	 */
	public static HashMap<String, Double> confidenceFunction(SqlObject sql,
			double percent) throws SQLException {
		System.out.println("Finding rare words ... with confidence function");

		// Input-Check
		if (percent <= 0) {
			System.out
					.println("Wrong input value \"percent\" found. Value is discarded and replaced by default value 0.5");
			percent = 0.5;
		}
		if (percent > 1) {
			percent = percent / 100;
			System.out
					.println("Wrong input value \"percent\" found. Value is replaced by "
							+ percent);
		}

		System.out.println("\tGet word frequencies");
		ResultSet resultset = sql
				.executeQuery("SELECT word,frequency FROM word_frequency order by frequency asc;");

		// Create HashTable
		HashMap<Long, Long> map = new HashMap<>();
		long count = 0;
		long frequency_saved = 0;
		long largest_Value = 0;
		//long largest_Key = 0;
		boolean newValue = true;
		while (resultset.next()) {
			long frequency = resultset.getLong("frequency");
			//System.out.println(resultset.getString("word") + "\t" + resultset.getLong("frequency"));
			if (newValue) {
				newValue = false;
				frequency_saved = frequency;
			}
			if (frequency == frequency_saved) {
				count++;
			} else {
				//System.out.println("\t\t comit " + frequency_saved + "\t" + count);
				map.put(frequency_saved, count);
				//log.log(Level.INFO, "({0},{1})", new Object[]{frequency_saved, count});

				if (largest_Value < count) {
					largest_Value = count;
					//largest_Key = frequency_saved;
				}
				count = 1;
				newValue = true;
			}
		}

		// get size of table
		ResultSet maxValue = sql.executeQuery("SELECT count(frequency) FROM word_frequency;");
		long size = 0;
		while (maxValue.next()) {
			size = maxValue.getLong("count(frequency)");
		}
		//System.out.println("size" + size);

		// Find Source Point
		long confidence_minus = 0;
		long confidence_plus = 0;
		long diff = Long.MAX_VALUE;
		long findValue = largest_Value / 1000;

		for (Map.Entry<Long, Long> hashEntry : map.entrySet()) {
			long value = hashEntry.getValue();
			//System.out.println("Print Hash\tKey " + hashEntry.getKey() + "\t" + "Value " + hashEntry.getValue());
			if (diff > Math.abs(findValue - value)) {
				diff = Math.abs(findValue - value);
				//System.out.println("\t\tNew Dif " + diff + " bei (" +hashEntry.getKey() + ", " + hashEntry.getValue() + ")");
				confidence_minus = hashEntry.getKey();
				confidence_plus = hashEntry.getKey();
			}
		}
		/*
		 System.out.println("Grosser Wert " + largest_Value);
		 System.out.println("1/3 Wert " + findValue);
		 System.out.println("Ausgangspunkt Mitte  " + confidence_minus);
		 System.out.println("Key " + confidence_minus + "\t" +
		 map.get(confidence_minus) );
		*/

		boolean repeat = false;
		while (count < percent * size) {
			do {
				repeat = false;
				if (confidence_minus > 1) {
					confidence_minus--;
					//System.out.println("Aussen " + confidence_minus);
					if (map.containsKey(confidence_minus)) {
						//System.out.println("\tInnen " + confidence_minus);
						count += map.get(confidence_minus);
					} else {
						repeat = true;
					}
				}
			} while (repeat);
			
			//int breakCounter=0;
			do {
				repeat=false;
				if (count < percent * size) {
					if(confidence_plus<largest_Value){
						confidence_plus++;
						
						if (map.containsKey(confidence_plus)) {
							//System.out.println("confidence plus " + confidence_plus);
							count += map.get(confidence_plus);
						} else {
							//breakCounter++;
							repeat=true;
						}
					}
				}
			} while (repeat);
            //} while (repeat && breakCounter<2);
		}
		
		System.out.println("\tIntermediate result: Lower confidence interval "
				+ confidence_minus + "; Upper confidence interval "
				+ confidence_plus + "\n");

		ResultSet words = sql
				.executeQuery("SELECT word,frequency FROM word_frequency where frequency >= "
						+ confidence_minus
						+ " AND frequency <= "
						+ confidence_plus + ";");
		while (words.next()) {
			double probability = ((double) words.getLong("frequency"))
					/ ((double) size);
			wordList.put(words.getString("word"), probability);
		}
		return wordList;
	}

	/**
	 * NormalizeCompleteWordCount
	 * 
	 * function gets complete wordcount and normalizes frequency-parameter
	 * 
	 * @param sql
	 *            objects which provides access to existing database system
	 * @return Hashmap with complete words
	 * @throws java.sql.SQLException
	 *             Thrown if query fails
	 */
	public static HashMap<String, Double> normalizeCompleteWordCount(
			SqlObject sql) throws SQLException {
		System.out.println("Normalizing  complete WordCountList");

		ResultSet maxValue = sql
				.executeQuery("SELECT max(frequency) FROM word_frequency;");
		long size = 0;
		while (maxValue.next()) {
			size = maxValue.getLong("max(frequency)");
		}

		ResultSet words = sql
				.executeQuery("SELECT word,frequency FROM word_frequency;");
		while (words.next()) {
			double probability = words.getLong("frequency") / size;
			wordList.put(words.getString("word"), probability);
		}
		return wordList;
	}
}
