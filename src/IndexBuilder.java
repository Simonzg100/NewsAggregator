import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class IndexBuilder implements IIndexBuilder {

    // <parseFeed> Parse each document/rss feed in the list and return a Map of 
    // each document and all the words in it. (punctuation and special
    // characters removed)
    //
    // @param feeds a List of rss feeds to parse
    // @return a Map of each documents (identified by its url) and the list of
    //         words in it.
    @Override
    public Map<String, List<String>> parseFeed(List<String> feeds) {
        Map<String, List<String>> map = new HashMap<>();
        for (String feedUrl : feeds) {
            try {
                // handle RSS file
                Document rssDocument = Jsoup.connect(feedUrl).get();
                Elements links = rssDocument.getElementsByTag("link");
                for (Element link : links) {
                    // deal with html file
                    // get the link text
                    String linkText = link.text(); // get the url
                    Document htmlDocument = Jsoup.connect(linkText).get();  // get the html document
                    Element body = htmlDocument.body(); // get the body
                    String text = body.text(); // get the text
                    // extract words from the text
                    List<String> words = extractWords(text);
                    // put the linkText and words into the map
                    map.put(linkText, words);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }
    
    /**
     * helper method to extract words from a string.
     */
    private List<String> extractWords(String text) {
        //Remove all punctuation and set everything to lowercase in the words
        //Split the string into an array of words using whitespace as a delimiter
        String[] words = text.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase().split("\\s+");
        return Arrays.asList(words);
    }

    // @param docs a map computed by {@parseFeed}
    // @return the forward index: a map of all documents and their
    //         tags/keywords. the key is the document, the value is a
    //         map of a tag term and its TFIDF value.
    //         The values (Map<String, Double>) are sorted
    //         by lexicographic order on the key (tag term)
    //
    @Override
    public Map<String, Map<String, Double>> buildIndex(Map<String, List<String>> docs) {
        Map<String, Map<String, Double>> map = new HashMap<>();
        // 1. calculate the number of documents
        int size = docs.size();
        
        for (Entry<String,List<String>> entrySet : docs.entrySet()) {
            String documentName = entrySet.getKey();
            List<String> words = entrySet.getValue();
            // sort the words by lexicographic order
            Collections.sort(words);

            int sumWords = words.size();
            // 2. calculate the times that each word appears in the document
            // the times that each word appears in the document
            Map<String, Integer> wordOneDoc = new HashMap<String, Integer>();
            // the number of documents that contain this word
            Map<String, Integer> wordTotalDoc = new HashMap<String, Integer>();
            // the TF-IDF value for each term in the document
            Map<String, Double> wordTfIdf = new TreeMap<String, Double>();

            // calculate the times that each word appears in the document
            // and the number of documents that contain this word
            for (String word : words) {
                if (wordOneDoc.containsKey(word)) {
                    wordOneDoc.put(word, wordOneDoc.get(word) + 1);
                } else {
                    wordOneDoc.put(word, 1);
                    // 3. calculate the number of documents that contain this word 
                    //   (the number of documents that contain this word is
                    //   the number of documents that contain this word in the first time)
                    int count = 0;
                    for (Entry<String,List<String>> entrySet2 : docs.entrySet()) {
                        List<String> words2 = entrySet2.getValue();
                        for (String word2 : words2) {
                            if (word2.equals(word)) {
                                count++;
                                break;
                            }
                        }
                    }
                    wordTotalDoc.put(word, count);
                }
            }

            // 4. calculate the TF-IDF value for each term in the document
            for (String word : words) {
                int wordCount = wordOneDoc.get(word);
                double tf = (double)wordCount / sumWords;
                double idf = Math.log((double)size / wordTotalDoc.get(word));
                double tfIdf = tf * idf;
                wordTfIdf.put(word, tfIdf);
            }
            map.put(documentName, wordTfIdf);
        }
        return map;
    }

    
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

    @Override
    public Map<?, ?> buildInvertedIndex(Map<String, Map<String, Double>> index) {
        // key- tag term, value- a Collection of Entry objects
        // mapping a document with the TFIDF value of the term
        Map<String, List<Entry<String, Double>>> map = new HashMap<>();
        // 1. swith the two key(string)
        for (Entry<String, Map<String, Double>> entrySet : index.entrySet()) {
            String documentName = entrySet.getKey();
            Map<String, Double> wordTfIdf = entrySet.getValue();
            for (Entry<String, Double> entrySet2 : wordTfIdf.entrySet()) {
                String word = entrySet2.getKey();
                Double tfIdf = entrySet2.getValue();
                if (map.containsKey(word)) {
                    map.get(word).add(new AbstractMap.SimpleEntry<>(documentName, tfIdf));
                } else {
                    List<Entry<String, Double>> list = new ArrayList<>();
                    list.add(new AbstractMap.SimpleEntry<>(documentName, tfIdf));
                    map.put(word, list);
                }
            }
        }
        // sort the value of the map by reverse tag term TFIDF value
        for (Entry<String, List<Entry<String, Double>>> entrySet : map.entrySet()) {
            // 2. get the value of the map
            List<Entry<String, Double>> list = entrySet.getValue();
            // 3. sort the value of the map by reverse tag term TFIDF value
            list.sort(new Comparator<Entry<String, Double>>() {
                @Override
                public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
        }
        return map;
    }

    /**
     *  homepage displays the tag terms and their associated articles. 
     * Tag terms are sorted by the number of articles. 
     * If two terms have the same number of articles, 
     * then they should be sorted by reverse lexicographic order.
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
    @Override
    public Collection<Entry<String, List<String>>> buildHomePage(Map<?, ?> invertedIndex) {
        List<Entry<String, List<String>>> list = new ArrayList<>();

        //1. remove the stop words. 
        Map<String, List<Entry<String, Double>>> filterIndex = new HashMap<>();
        for (Entry<?, ?> entrySet : invertedIndex.entrySet()) {
            String tagTerm = (String) entrySet.getKey();
            List<Entry<String, Double>> value =  (List<Entry<String, Double>>) entrySet.getValue();
            if (!STOPWORDS.contains(tagTerm)) {
                filterIndex.put(tagTerm, value);
            }
        }

        // 2. build the home page
        for (Entry<String,List<Entry<String,Double>>> entrySet : filterIndex.entrySet()) {
            String tagTerm = entrySet.getKey();
            List<Entry<String, Double>> value = (List<Entry<String, Double>>) entrySet.getValue();
            List<String> articles = new ArrayList<>();
            for (Entry<String, Double> doc: value) {
                String article = doc.getKey();
                articles.add(article);
            }
            list.add(new AbstractMap.SimpleEntry<>(tagTerm, articles));
        }

        // 3. sort the list -- 
        //      1. Tag terms are sorted by the number of articles.
        //      2. If two terms have the same number of articles,
        //      then they should be sorted by reverse lexicographic order.
        list.sort(new Comparator<Entry<String, List<String>>>() {
            @Override
            public int compare(Entry<String, List<String>> o1, Entry<String, List<String>> o2) {
                int sizeComparison = Integer.compare(o2.getValue().size(), o1.getValue().size());
                if (sizeComparison == 0) {
                    return o2.getKey().compareTo(o1.getKey());
                }
                return sizeComparison;
            }
        });
        return list;
    }

    /**
     * Create a file containing all the words in the inverted index. Each word
     * should occupy a line Words should be written in lexicographic order
     * assign a weight of 0 to each word. The method must store the words into a 
     * file named autocomplete.txt
     * 
     * @param homepage the collection used to generate the homepage (buildHomePage)
     * @return A collection containing all the words written
     * into the file sorted by lexicographic order
     */
    @Override
    public Collection<?> createAutocompleteFile(Collection<Entry<String, List<String>>> homepage) {

        Set<String> wordsSet = new TreeSet<>();
        // 1. Extract all words from the homepage
        for (Entry<String, List<String>> entry : homepage) {
            String tagTerm = entry.getKey();
            wordsSet.add(tagTerm);
        }

        // 2. write each word to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("autocomplete.txt"))) {
            // Write the number of words in the first line
            writer.write(Integer.toString(wordsSet.size()));
            writer.newLine();

            for (String word : wordsSet) {
                writer.write("   0 " + word);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return wordsSet;
    }

    /*
     * The users should be able to enter a query term and our news aggregator will 
     * return all the articles related (tagged) to that term. 
     * The relevant articles are retrieved from the inverted index.
     */
    @Override
    public List<String> searchArticles(String queryTerm, Map<?, ?> invertedIndex) {
        // 1. traverse the map using entry set
        // 2. get the key -- value <map>
            // check if the key exists.
        // 3. iterate the map, and then add the document's name to the list
        List<String> res =  new LinkedList<>();
        List<Entry<String, Double>> value =
                (List<Entry<String, Double>>) invertedIndex.get(queryTerm);
        if (value == null) {
            return res;
        } else {
            for (Entry<String, Double> entry : value) {
                res.add(entry.getKey());
            }
        }
        return res;
    }
}
        
