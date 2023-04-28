import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author ericfouh
 */
public class NewsAggregatorGUI
{

    private JFrame               frame;
    private AutocompletePanel searchBox;
    private JComboBox            rssBox;
    private IIndexBuilder        idxBuilder;
    public static final String[] rssUrls      =
        { "https://rss.nytimes.com/services/xml/rss/nyt/US.xml",
            "http://feeds.washingtonpost.com/rss/rss_powerpost",
            "http://rss.cnn.com/rss/cnn_us.rss",
            "http://feeds.foxnews.com/foxnews/latest",
            "http://feeds.bbci.co.uk/news/world/rss.xml",
            "http://rss.cnn.com/rss/cnn_topstories.rss",
            "http://localhost:8090/sample_rss_feed.xml" };

    /**
     * 
     */
    private Map<?, ?>            invIdx;
    private boolean              autocomplete = false;


    /**
     * Launch the application.
     */
    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable() {
            public void run()
            {
                try
                {
                    NewsAggregatorGUI window = new NewsAggregatorGUI();
                    window.frame.setVisible(true);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * Create the application.
     */
    public NewsAggregatorGUI()
    {
        initialize();
    }


    private void initRSSList()
    {
        idxBuilder = new IndexBuilder();

        rssBox = new JComboBox(rssUrls);
        rssBox.setSelectedIndex(0);

    }


    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JButton btnSearch = new JButton("Search");
        btnSearch.setBounds(350, 135, 90, 30);
        btnSearch.setEnabled(false);
        frame.getContentPane().add(btnSearch);

        DefaultListModel<String> articlesList = new DefaultListModel<String>();
        JList results = new JList(articlesList);
        // results.setBounds(0, 270, 450, 300);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(results);
        results.setLayoutOrientation(JList.VERTICAL);
        scrollPane.setVisible(true);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane);
        panel.setBounds(0, 270, 450, 300);

        frame.add(panel);
        initRSSList();

        rssBox.setBounds(0, 0, 350, 27);
        frame.getContentPane().add(rssBox);

        JButton btnAddRSS = new JButton("Add RSS");
        btnAddRSS.setBounds(350, 1, 90, 29);
        frame.getContentPane().add(btnAddRSS);

        JButton btnIndex = new JButton("Create Indexes");
        btnIndex.setBounds(0, 32, 117, 29);
        btnIndex.setEnabled(false);
        frame.getContentPane().add(btnIndex);

        JButton btnHome = new JButton("Home Page");
        btnHome.setBounds(129, 32, 117, 29);
        btnHome.setEnabled(false);
        frame.getContentPane().add(btnHome);

        JButton btnAutoCplt = new JButton("Autocomplete");
        btnAutoCplt.setToolTipText("Update autocomplete file");
        btnAutoCplt.setBounds(250, 32, 117, 29);
        btnAutoCplt.setEnabled(false);
        frame.getContentPane().add(btnAutoCplt);

        searchBox = new AutocompletePanel("autocomplete.txt");
        searchBox.setBounds(0, 135, 350, 130);
        searchBox.setVisible(true);
        frame.getContentPane().add(searchBox);

        DefaultListModel listModel = new DefaultListModel();
        JList rssList = new JList(listModel);
        rssList.setToolTipText("News Sources");
        rssList.setEnabled(false);
        rssList.setBounds(10, 59, 350, 75);

        frame.getContentPane().add(rssList);

        // add RSS
        btnAddRSS.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                btnIndex.setEnabled(true);
                String selected = (String)rssBox.getSelectedItem();
                if (!listModel.contains(selected))
                {
                    // limit 4 sources
                    if (listModel.size() == 4)
                    {
                        listModel.remove(0);
                    }
                    listModel.addElement(selected);
                }

            }

        });
        // index building
        btnIndex.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Convert listmodel to a List
                List<String> feeds = new ArrayList<>(listModel.size());
                for (int i = 0; i < listModel.size(); i++)
                    feeds.add((String)listModel.get(i));
                Map<String, List<String>> map = idxBuilder.parseFeed(feeds);
                Map<String, Map<String, Double>> index =
                    idxBuilder.buildIndex(map);
                invIdx = idxBuilder.buildInvertedIndex(index);
                btnHome.setEnabled(true);
                btnSearch.setEnabled(true);
                btnAutoCplt.setEnabled(true);
            }
        });

        // Home page
        btnHome.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Map<String, List<Entry<String, Double>>> articles =
                    (Map<String, List<Entry<String, Double>>>)invIdx;
                Collection<Entry<String, List<String>>> home =
                    (Collection<Entry<String, List<String>>>)idxBuilder
                        .buildHomePage(invIdx);
                if (home.size() > 0)
                {
                    articlesList.clear();
                }
                Iterator<Entry<String, List<String>>> iter = home.iterator();
                while (iter.hasNext())
                {
                    Entry<String, List<String>> entry = iter.next();
                    articlesList.addElement(entry.getKey());
                    for (String url : entry.getValue())
                        articlesList.addElement("\t\t" + url);
                }
            }
        });

        // Autocomplete
        btnAutoCplt.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                idxBuilder
                    .createAutocompleteFile(idxBuilder.buildHomePage(invIdx));
                searchBox = new AutocompletePanel("autocomplete.txt");
            }

        });

        // search
        btnSearch.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {

                String query = searchBox.getSearchText();
                if (query.length() > 0)
                {

                    List<String> articles =
                        idxBuilder.searchArticles(query, invIdx);
                    if (articles != null && articles.size() > 0)
                    {
                        articlesList.clear();
                        articlesList.addElement(query);
                        for (String url : articles)
                            articlesList.addElement("\t\t" + url);
                    }
                }
            }
        });

        // clickable results
        results.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt)
            {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() == 1)
                {

                    // index of the article
                    int index = list.locationToIndex(evt.getPoint());
                    String url = articlesList.get(index);
                    URI uriAddress;
                    try
                    {
                        URI uri = new URI(url.trim());
                        if (url.contains("http://"))
                            Desktop.getDesktop().browse(uri);
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (URISyntaxException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        });

    }
}
