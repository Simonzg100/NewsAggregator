 

import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test; 

import static org.junit.Assert.*;
/** 
* IndexBuilder Tester. 
* 
* @author <Authors name> 
* @since <pre>Apr 1, 2023</pre> 
* @version 1.0 
*/ 
public class IndexBuilderTest { 
    IndexBuilder indexBuilder;
    List<String> feeds;
    Map<String,List<String>> parseFeed;
    Map<String, Map<String, Double>> buildIndexMap;
    @Before
    public void before() throws Exception {
        indexBuilder = new IndexBuilder();
        feeds = new ArrayList<>();
        feeds.add("https://www.cis.upenn.edu/~cit5940/sample_rss_feed.xml");
    }

    @After
    public void after() throws Exception {
    }

    /**
    *
    * Method: parseFeed(List<String> feeds)
    *
    */
    @Test
    public void testParseFeed() throws Exception {

        //Your map contains the correct number of terms in the lists (values)
        parseFeed = indexBuilder.parseFeed(feeds);
        int size = parseFeed.size();
        // test map has the correct number of files
        assertEquals(5, parseFeed.size());
        // test map has the correct number of terms in each file
        assertTrue(parseFeed.containsKey("https://www.seas.upenn.edu/~cit5940/page1.html"));
        List<String> strings = parseFeed.get("https://www.seas.upenn.edu/~cit5940/page1.html");
        int termSize = strings.size();
        assertEquals(10,termSize);
    }

    /**
    *
    * Method: buildIndex(Map<String, List<String>> docs)
    *
    */
    @Test
    public void testBuildIndex() throws Exception {
        parseFeed = indexBuilder.parseFeed(feeds);
        buildIndexMap = indexBuilder.buildIndex(parseFeed);
        assertEquals(5, buildIndexMap.size());
        // test the page 1
        Map<String, Double> page1
                = buildIndexMap.get("https://www.seas.upenn.edu/~cit5940/page1.html");
        // test the page 1's number of terms.
        assertEquals(8, page1.size());
        // test the term td-idf
        Double data = page1.get("data");
        assertEquals(0.1021, data,0.0001);
        data = page1.get("structures");
        assertEquals(0.183, data,0.001);
    }

    /**
    *
    * Method: buildInvertedIndex(Map<String, Map<String, Double>> index)
    *
    */
    @Test
    public void testBuildInvertedIndex() throws Exception {
        parseFeed = indexBuilder.parseFeed(feeds);
        buildIndexMap = indexBuilder.buildIndex(parseFeed);
        Map<?, ?> map = indexBuilder.buildInvertedIndex(buildIndexMap);
        // test the size
        int size = map.size();
        assertEquals(92,size);
        List<Map.Entry<String, Double>> list = (List<Map.Entry<String, Double>>) map.get("about");
        assertEquals(1, list.size());
        String expectedHtml = "https://www.seas.upenn.edu/~cit5940/page5.html";
        assertEquals(expectedHtml,list.get(0).getKey());
        Double expected  = 0.0894;
        assertEquals(expected, list.get(0).getValue(),0.0001);
    }

    /**
    *
    * Method: buildHomePage(Map<?, ?> invertedIndex)
    *
    */
    @Test
    public void testBuildHomePage() throws Exception {
        parseFeed = indexBuilder.parseFeed(feeds);
        buildIndexMap = indexBuilder.buildIndex(parseFeed);
        Map<?, ?> invertedIndex = indexBuilder.buildInvertedIndex(buildIndexMap);
        Collection<Map.Entry<String, List<String>>> homePage =
                indexBuilder.buildHomePage(invertedIndex);
        // test the home page size
        assertEquals(57, homePage.size());
        for (Map.Entry<String, List<String>> stringListEntry : homePage) {
            String key = stringListEntry.getKey();
            List<String> value = stringListEntry.getValue();
            switch (key) {
                case "data":
                    assertEquals(3, value.size());
                    assertEquals("https://www.seas.upenn.edu/~cit5940/page1.html",value.get(0));
                    break;
                case "trees":
                    assertEquals(2, value.size());
                    assertEquals("https://www.seas.upenn.edu/~cit5940/page3.html", value.get(0));
                    break;
                default:
                    break;
            }
        }
    }

    /**
    *
    * Method: createAutocompleteFile(Collection<Entry<String, List<String>>> homepage)
    *
    */
    @Test
    public void testCreateAutocompleteFile() throws Exception {
        parseFeed = indexBuilder.parseFeed(feeds);
        buildIndexMap = indexBuilder.buildIndex(parseFeed);
        Map<?, ?> invertedIndex = indexBuilder.buildInvertedIndex(buildIndexMap);
        Collection<Map.Entry<String, List<String>>> homePage
                = indexBuilder.buildHomePage(invertedIndex);
        Collection<?> autocompleteFile = indexBuilder.createAutocompleteFile(homePage);
        assertEquals(57, autocompleteFile.size());
        assertTrue(autocompleteFile.contains("data"));

    }

    /**
    *
    * Method: searchArticles(String queryTerm, Map<?, ?> invertedIndex)
    *
    */
    @Test
    public void testSearchArticles() throws Exception {
        parseFeed = indexBuilder.parseFeed(feeds);
        buildIndexMap = indexBuilder.buildIndex(parseFeed);
        Map<?, ?> invertedIndex = indexBuilder.buildInvertedIndex(buildIndexMap);
        String query = "data";
        List<String> strings = indexBuilder.searchArticles(query, invertedIndex);
        assertEquals(3, strings.size());
        assertTrue(strings.contains("https://www.seas.upenn.edu/~cit5940/page1.html"));
    }

} 
