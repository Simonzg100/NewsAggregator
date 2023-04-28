import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author ericfouh
 */
public interface IIndexBuilder
{
    /**
     * <parseFeed> Parse each document/rss feed in the list and return a Map of
     * each document and all the words in it. (punctuation and special
     * characters removed)
     * 
     * @param feeds a List of rss feeds to parse
     * @return a Map of each documents (identified by its url) and the list of
     *         words in it.
     */
    public Map<String, List<String>> parseFeed(List<String> feeds);


    /**
     * @param docs a map computed by {@parseFeed}
     * @return the forward index: a map of all documents and their 
     *         tags/keywords. the key is the document, the value is a 
     *         map of a tag term and its TFIDF value. 
     *         The values (Map<String, Double>) are sorted
     *         by lexicographic order on the key (tag term)
     *  
     */
    public Map<String, Map<String, Double>> buildIndex(
        Map<String, List<String>> docs);


    /**
     * Build an inverted index consisting of a map of each tag term and a Collection (Java)
     * of Entry objects mapping a document with the TFIDF value of the term 
     * (for that document)
     * The Java collection (value) is sorted by reverse tag term TFIDF value 
     * (the document in which a term has the
     * highest TFIDF should be listed first).
     * 
     * 
     * @param index the index computed by {@buildIndex}
     * @return inverted index - a sorted Map of the documents in which term is a keyword
     */

    public Map<?, ?> buildInvertedIndex(Map<String, Map<String, Double>> index);


    /**
     * @param invertedIndex
     * @return a sorted collection of terms and articles Entries are sorted by
     *         number of articles. If two terms have the same number of 
     *         articles, then they should be sorted by reverse lexicographic order.
     *         The Entry class is the Java abstract data type
     *         implementation of a tuple
     *         https://docs.oracle.com/javase/9/docs/api/java/util/Map.Entry.html
     *         One useful implementation class of Entry is
     *         AbstractMap.SimpleEntry
     *         https://docs.oracle.com/javase/9/docs/api/java/util/AbstractMap.SimpleEntry.html
     */
    public Collection<Entry<String, List<String>>> buildHomePage(
        Map<?, ?> invertedIndex);


    /**
     * Create a file containing all the words in the inverted index. Each word
     * should occupy a line Words should be written in lexicographic order
     * assign a weight of 0 to each word. The method must store the words into a 
     * file named autocomplete.txt
     * 
     * @param homepage the collection used to generate the homepage (buildHomePage)
     * @return A collection containing all the words written into the file sorted by lexicographic order
     */
    public Collection<?> createAutocompleteFile(Collection<Entry<String, List<String>>> homepage); 

    /**
     * @param queryTerm
     * @param invertedIndex
     * @return
     */
    public List<String> searchArticles(
        String queryTerm,
        Map<?, ?> invertedIndex);

    // Stop words
    public static String[] STOPW = { "a", "about", "above", "across",
        "after", "afterwards", "again", "against", "all", "almost", "alone",
        "along", "already", "also", "although", "always", "am", "among",
        "amongst", "amoungst", "amount", "an", "and", "another", "any",
        "anyhow", "anyone", "anything", "anyway", "anywhere", "are", "around",
        "as", "at", "back", "be", "became", "because", "become", "becomes",
        "becoming", "been", "before", "beforehand", "behind", "being", "below",
        "beside", "besides", "between", "beyond", "bill", "both", "bottom",
        "but", "by", "call", "can", "cannot", "cant", "co", "computer", "con",
        "could", "couldnt", "cry", "de", "describe", "detail", "do", "done",
        "down", "due", "during", "each", "eg", "eight", "either", "eleven",
        "else", "elsewhere", "empty", "enough", "etc", "even", "ever", "every",
        "everyone", "everything", "everywhere", "except", "few", "fifteen",
        "fify", "fill", "find", "fire", "first", "five", "for", "former",
        "formerly", "forty", "found", "four", "from", "front", "full",
        "further", "get", "give", "go", "had", "has", "hasnt", "have", "he",
        "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon",
        "hers", "herse", "him", "himse", "his", "how", "however", "hundred",
        "i", "ie", "if", "in", "inc", "indeed", "interest", "into", "is", "it",
        "its", "itse", "keep", "last", "latter", "latterly", "least", "less",
        "ltd", "made", "many", "may", "me", "meanwhile", "might", "mill",
        "mine", "more", "moreover", "most", "mostly", "move", "much", "must",
        "my", "myse", "name", "namely", "neither", "never", "nevertheless",
        "next", "nine", "no", "nobody", "none", "noone", "nor", "not",
        "nothing", "now", "nowhere", "of", "off", "often", "on", "once", "one",
        "only", "onto", "or", "other", "others", "otherwise", "our", "ours",
        "ourselves", "out", "over", "own", "part", "per", "perhaps", "please",
        "put", "rather", "re", "same", "see", "seem", "seemed", "seeming",
        "seems", "serious", "several", "she", "should", "show", "side", "since",
        "sincere", "six", "sixty", "so", "some", "somehow", "someone",
        "something", "sometime", "sometimes", "somewhere", "still", "such",
        "system", "take", "ten", "than", "that", "the", "their", "them",
        "themselves", "then", "thence", "there", "thereafter", "thereby",
        "therefore", "therein", "thereupon", "these", "they", "thick", "thin",
        "third", "this", "those", "though", "three", "through", "throughout",
        "thru", "thus", "to", "together", "too", "top", "toward", "towards",
        "twelve", "twenty", "two", "un", "under", "until", "up", "upon", "us",
        "very", "via", "was", "we", "well", "were", "what", "whatever", "when",
        "whence", "whenever", "where", "whereafter", "whereas", "whereby",
        "wherein", "whereupon", "wherever", "whether", "which", "while",
        "whither", "who", "whoever", "whole", "whom", "whose", "why", "will",
        "with", "within", "without", "would", "yet", "you", "your", "yours",
        "yourself", "yourselves", };
    public static HashSet<String> STOPWORDS = new HashSet<>(Arrays.asList(STOPW));

}
