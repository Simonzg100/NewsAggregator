import java.util.List;

/**
 * @author ericfouh
 */
public interface IAutocomplete
{

    /**
     * Adds a new word with its associated weight to the Trie
     * 
     * @param word the word to be added to the Trie
     * @param weight the weight of the word
     */
    public void addWord(String word, long weight);


    /**
     * Initializes the Trie
     *
     * @param filename the file to read all the autocomplete data from each line
     *                 contains a word and its weight This method will call the
     *                 addWord method
     * @param k the maximum number of suggestions that should be displayed 
     * @return the root of the Trie You might find the readLine() method in
     *         BufferedReader useful in this situation as it will allow you to
     *         read a file one line at a time.
     */
    public Node buildTrie(String filename, int k);

    
    /**
     * @return k the the maximum number of suggestions that should be displayed 
     */
    public int numberSuggestions();

    /**
     * @param prefix
     * @return the root of the subTrie corresponding to the last character of
     *         the prefix.
     */
    public Node getSubTrie(String prefix);


    /**
     * @param prefix
     * @return the number of words that start with prefix.
     */
    public int countPrefixes(String prefix);


    /**
     * This method should not throw an exception
     * @param prefix
     * @return a List containing all the ITerm objects with query starting with
     *         prefix. Return an empty list if there are no ITerm object starting
     *         with prefix.
     */
    public List<ITerm> getSuggestions(String prefix);

}
