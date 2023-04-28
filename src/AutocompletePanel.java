import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;

public class AutocompletePanel
extends JPanel
{
// for serializable classes
private static final long serialVersionUID = 1L;

private final JTextField  searchText;                            // the
                                                                 // search
                                                                 // bar
private Autocomplete              auto;                                  // the
                                                                 // Autocomplete
                                                                 // object
private String[]          results          = new String[10];      // an
                                                                 // array
                                                                 // of
                                                                 // matches
//// private JList<String> suggestions; // a list of autocomplete
//// matches (Java 7)
private JList             suggestions;                           // a
                                                                 // list
                                                                 // of
                                                                 // autocomplete
                                                                 // matches
                                                                 // (Java
                                                                 // 6)
private JScrollPane       scrollPane;                            // the
                                                                 // scroll
                                                                 // bar
                                                                 // on
                                                                 // the
                                                                 // side
                                                                 // of
                                                                 // the
private JPanel            suggestionsPanel;                      // the
                                                                 // dropdown
                                                                 // menu
                                                                 // of
                                                                 // suggestions
private int               extraMargin      = 5;                  // extra
                                                                 // room
                                                                 // to
                                                                 // leave
                                                                 // at
                                                                 // the
                                                                 // bottom
                                                                 // of
                                                                 // the
                                                                 // suggestion
                                                                 // drop-down
                                                                 // below
                                                                 // the
                                                                 // last
                                                                 // suggestion

// Note: can't use JList<String> in Java 6

// TODO: change how this is implemented so it is dynamic;
// shouldn't have to define a column number.

// Keep these next two values in sync! - used to keep the search box
// the same width as the drop-down
// DEF_COLUMNS should be the number of characters in suggListLen

// number of columns in the search text that is kept
private final int         DEF_COLUMNS      = 45;

// an example of one of the longest strings in the database
private final String      suggListLen      =
    "<b>Harry Potter and the Deathly Hallows: Part 1 (2010)</b>";


/**
 * Creates the Autocomplete object and the search bar and suggestion
 * drop-down portions of the GUI
 *
 * @param filename
 *            the file the Autocomplete object is constructed from
 */
public AutocompletePanel(String filename)
{
    super();

    


    auto = new Autocomplete();
    auto.buildTrie(filename, 6);


    GroupLayout layout = new GroupLayout(this);
    this.setLayout(layout);

    // create the search text, and allow the user to interact with it
    searchText = new JTextField(DEF_COLUMNS);
    searchText.setMaximumSize(
        new Dimension(
            searchText.getMaximumSize().width,
            searchText.getPreferredSize().height));
    searchText.getInputMap().put(KeyStroke.getKeyStroke("UP"), "none");
    searchText.getInputMap()
        .put(KeyStroke.getKeyStroke("DOWN"), "none");
    searchText.addFocusListener(new FocusListener() {
        @Override
        public void focusGained(FocusEvent e)
        {
            int pos = searchText.getText().length();
            searchText.setCaretPosition(pos);
        }


        public void focusLost(FocusEvent e)
        {
        }
    });

    // create the search text box
    JPanel searchTextPanel = new JPanel();
    searchTextPanel.add(searchText);
    searchTextPanel
        .setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    searchTextPanel.setLayout(new GridLayout(1, 1));

    // create the drop-down menu items
    int fontsize = 13;
    int cellHeight = 20;

    // suggestions = new JList<String>(results);
    suggestions = new JList(results);
    suggestions
        .setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    suggestions.setVisible(false);
    suggestions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    suggestions.setMaximumSize(
        new Dimension(
            searchText.getMaximumSize().width,
            suggestions.getPreferredSize().height));

    // Set to make equal to the width of the textfield
    suggestions.setPrototypeCellValue(suggListLen);
    suggestions.setFont(
        suggestions.getFont().deriveFont(Font.PLAIN, fontsize));
    suggestions.setFixedCellHeight(cellHeight);

    // add arrow-key interactivity to the drop-down menu items
    Action makeSelection = new AbstractAction() {
        // for serializable classes
        private static final long serialVersionUID = 1L;


        public void actionPerformed(ActionEvent e)
        {
            if (!suggestions.isSelectionEmpty())
            {
                String selection =
                    (String)suggestions.getSelectedValue();
                
                selection = selection.replaceAll("\\<.*?>", "");
                searchText.setText(selection);
                getSuggestions(selection);
            }
        }
    };
    Action moveSelectionUp = new AbstractAction() {
        // for serializable classes
        private static final long serialVersionUID = 1L;


        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (suggestions.getSelectedIndex() >= 0)
            {
                suggestions.requestFocusInWindow();
                suggestions.setSelectedIndex(
                    suggestions.getSelectedIndex() - 1);
            }
        }
    };
    Action moveSelectionDown = new AbstractAction() {
        // for serializable classes
        private static final long serialVersionUID = 1L;


        public void actionPerformed(ActionEvent e)
        {
            if (suggestions.getSelectedIndex() != results.length)
            {
                suggestions.requestFocusInWindow();
                suggestions.setSelectedIndex(
                    suggestions.getSelectedIndex() + 1);
            }
        }
    };
    Action moveSelectionUpFocused = new AbstractAction() {
        // for serializable classes
        private static final long serialVersionUID = 1L;


        public void actionPerformed(ActionEvent e)
        {
            if (suggestions.getSelectedIndex() == 0)
            {
                suggestions.clearSelection();
                searchText.requestFocusInWindow();
                searchText.setSelectionEnd(0);
            }
            else if (suggestions.getSelectedIndex() >= 0)
            {
                suggestions.setSelectedIndex(
                    suggestions.getSelectedIndex() - 1);
            }
        }
    };
    suggestions.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke("UP"), "moveSelectionUp");
    suggestions.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke("DOWN"), "moveSelectionDown");
    suggestions.getActionMap().put("moveSelectionUp", moveSelectionUp);
    suggestions.getActionMap()
        .put("moveSelectionDown", moveSelectionDown);
    suggestions.getInputMap(JComponent.WHEN_FOCUSED)
        .put(KeyStroke.getKeyStroke("ENTER"), "makeSelection");
    suggestions.getInputMap()
        .put(KeyStroke.getKeyStroke("UP"), "moveSelectionUpFocused");
    suggestions.getActionMap()
        .put("moveSelectionUpFocused", moveSelectionUpFocused);
    suggestions.getActionMap().put("makeSelection", makeSelection);

    // Create the suggestion drop-down panel and scroll bar
    suggestionsPanel = new JPanel();

    scrollPane = new JScrollPane(suggestions);
    scrollPane.setVisible(false);
    int prefBarWidth =
        scrollPane.getVerticalScrollBar().getPreferredSize().width;
    suggestions.setPreferredSize(
        new Dimension(searchText.getPreferredSize().width, 0));
    scrollPane.setAutoscrolls(true);
    scrollPane.setHorizontalScrollBarPolicy(
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setVerticalScrollBarPolicy(
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

    // resize widths and heights of all components to fit nicely
    int preferredWidth =
        searchText.getPreferredSize().width + 2 * prefBarWidth;
    int maxWidth = searchText.getMaximumSize().width + 2 * prefBarWidth;
    int searchBarHeight = searchText.getPreferredSize().height;
    int suggestionHeight = suggestions.getFixedCellHeight();
    int maxSuggestionHeight = 200;

    suggestionsPanel.setPreferredSize(
        new Dimension(preferredWidth, suggestionHeight));
    suggestionsPanel
        .setMaximumSize(new Dimension(maxWidth, maxSuggestionHeight));
    suggestionsPanel
        .setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    suggestionsPanel.add(scrollPane);
    suggestionsPanel.setLayout(new GridLayout(1, 1));

    this.setPreferredSize(
        new Dimension(preferredWidth, this.getPreferredSize().height));
    this.setMaximumSize(
        new Dimension(
            preferredWidth,
            searchBarHeight + maxSuggestionHeight));

    searchTextPanel.setPreferredSize(
        new Dimension(preferredWidth, searchBarHeight));
    searchTextPanel
        .setMaximumSize(new Dimension(maxWidth, searchBarHeight));
    searchText.setMaximumSize(new Dimension(maxWidth, searchBarHeight));

    // add mouse interactivity with the drop-down menu
    suggestions.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent mouseEvent)
        {
            JList theList = (JList)mouseEvent.getSource();
            if (mouseEvent.getClickCount() >= 1)
            {
                int index =
                    theList.locationToIndex(mouseEvent.getPoint());
                if (index >= 0)
                {
                    String selection = getSelectedText();
                    searchText.setText(selection);
                    String text = searchText.getText();
                    getSuggestions(text);
                }
            }
        }


        @Override
        public void mouseEntered(MouseEvent mouseEvent)
        {
            JList theList = (JList)mouseEvent.getSource();
            int index = theList.locationToIndex(mouseEvent.getPoint());
            theList.requestFocusInWindow();
            theList.setSelectedIndex(index);
        }


        @Override
        public void mouseExited(MouseEvent mouseEvent)
        {
            suggestions.clearSelection();
            searchText.requestFocusInWindow();
        }
    });
    suggestions.addMouseMotionListener(new MouseInputAdapter() {
        @Override

        // Google a term when a user clicks on the dropdown menu
        public void mouseClicked(MouseEvent mouseEvent)
        {
            JList theList = (JList)mouseEvent.getSource();
            if (mouseEvent.getClickCount() >= 1)
            {
                int index =
                    theList.locationToIndex(mouseEvent.getPoint());
                if (index >= 0)
                {
                    String selection = getSelectedText();
                    searchText.setText(selection);
                    String text = searchText.getText();
                    getSuggestions(text);
                }
            }
        }


        @Override
        public void mouseEntered(MouseEvent mouseEvent)
        {
            JList theList = (JList)mouseEvent.getSource();
            int index = theList.locationToIndex(mouseEvent.getPoint());
            theList.requestFocusInWindow();
            theList.setSelectedIndex(index);
        }


        @Override
        public void mouseMoved(MouseEvent mouseEvent)
        {
            JList theList = (JList)mouseEvent.getSource();
            int index = theList.locationToIndex(mouseEvent.getPoint());
            theList.requestFocusInWindow();
            theList.setSelectedIndex(index);
        }
    });

    // add a listener that allows updates each time the user types
    searchText.getDocument()
        .addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)
            {
                changedUpdate(e);
            }


            public void removeUpdate(DocumentEvent e)
            {
                changedUpdate(e);
            }


            public void changedUpdate(DocumentEvent e)
            {
                String text = searchText.getText().trim();

                // updates the drop-down menu
                getSuggestions(text);
                updateListSize();
            }
        });

    // When a user clicks on a suggestion, Google it
    searchText.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e)
        {
            String selection = getSelectedText();
            searchText.setText(selection);
            getSuggestions(selection);
        }
    });

    // Define the layout of the text box and suggestion dropdown
    layout.setHorizontalGroup(
        layout.createSequentialGroup().addGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(
                    searchTextPanel,
                    0,
                    GroupLayout.DEFAULT_SIZE,
                    GroupLayout.PREFERRED_SIZE)
                .addComponent(
                    suggestionsPanel,
                    GroupLayout.DEFAULT_SIZE,
                    GroupLayout.DEFAULT_SIZE,
                    GroupLayout.PREFERRED_SIZE))

    );

    layout.setVerticalGroup(
        layout.createSequentialGroup().addComponent(searchTextPanel)
            .addComponent(suggestionsPanel));
}


/**
 * Re-populates the drop-down menu with the new suggestions, and resizes
 * the containing panel vertically
 */
private void updateListSize()
{
    int rows = 5;
    if (suggestions.getModel().getSize() < 5)
    {
        rows = suggestions.getModel().getSize();
    }

    int suggWidth = searchText.getPreferredSize().width;
    int suggPanelWidth = suggestionsPanel.getPreferredSize().width;
    int suggHeight = rows * suggestions.getFixedCellHeight();

    suggestions.setPreferredSize(new Dimension(suggWidth, suggHeight));
    suggestionsPanel.setPreferredSize(
        new Dimension(suggPanelWidth, suggHeight + extraMargin));
    suggestionsPanel.setMaximumSize(
        new Dimension(suggPanelWidth, suggHeight + extraMargin));

    // redraw the suggestion panel
    suggestionsPanel.setVisible(false);
    suggestionsPanel.setVisible(true);
}


// see getSuggestions for documentation
public void update()
{
    getSuggestions(searchText.getText());
}


/**
 * Makes a call to the implementation of Autocomplete to get suggestions
 * for the currently entered text.
 *
 * @param text
 *            string to search for
 */
public void getSuggestions(String text)
{

    // don't search for suggestions if there is no input
    if (text.equals(""))
    {
        suggestions.setListData(new String[0]);
        suggestions.clearSelection();
        suggestions.setVisible(false);
        scrollPane.setVisible(false);
    }
    else
    {
        text = text.trim();

        int textLen = text.length();
        // clear the previous match
        List<ITerm>matches = new ArrayList<ITerm>();
        matches = auto.getSuggestions(text);
        

         Collections
         .sort(matches, ITerm.byReverseWeightOrder());


         Term[] allResults = matches.toArray(new Term[matches.size()]);

        if (allResults == null)
        {
            throw new NullPointerException("allMatches() is null");
        }

        results = new String[Math.min(10, allResults.length)];
        if (Math.min(10, allResults.length) > 0)
        {
            for (int i = 0; i < results.length; i++)
            {

                // A bit of a hack to get the Term's query string
                // and weight from toString()
                String next = allResults[i].toString();

                if (allResults[i] == null)
                {
                    throw new NullPointerException(
                        "allMatches() "
                            + "returned an array with a null entry");
                }
                int tab = next.indexOf('\t');
                if (tab < 0)
                {
                    throw new RuntimeException(
                        "allMatches() returned"
                            + " an array with an entry without a tab:"
                            + " '" + next + "'");
                }
                String weight = next.substring(0, tab).trim();
                String query = next.substring(tab).trim();

                // truncate length if needed
                if (query.length() > suggListLen.length())
                    query = query.substring(0, suggListLen.length());

                // create the table HTML
                results[i] = "<html><table width=\""
                    + searchText.getPreferredSize().width + "\">"
                    + "<tr><td align=left>"
                    + query.substring(0, textLen) + "<b>"
                    + query.substring(textLen) + "</b>";
                
                results[i] += "</table></html>";
            }
            suggestions.setListData(results);
            suggestions.setVisible(true);
            scrollPane.setVisible(true);
        }
        else
        {
            // No suggestions
            suggestions.setListData(new String[0]);
            suggestions.clearSelection();
            suggestions.setVisible(false);
            scrollPane.setVisible(false);
        }
    }
}


// bring the clicked suggestion up to the Search bar and search it
public String getSelectedText()
{
    if (!suggestions.isSelectionEmpty())
    {
        String selection = (String)suggestions.getSelectedValue();
        
        selection = selection.replaceAll("\\<.*?>", "");
        selection = selection.replaceAll("^[ \t]+|[ \t]+$", "");
        return selection;
    }
    else
    {
        return getSearchText();
    }
}


public String getSearchText()
{
    return searchText.getText();
}
}
