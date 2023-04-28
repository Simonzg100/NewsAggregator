import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Autocomplete implements IAutocomplete {

    private final Node root;

    public Autocomplete() {
        root = new Node();
    }

    public Node getRoot() {
        return root;
    }

    @Override
    public void addWord(String word, long weight) {
        if (word == null || word.isEmpty()) {
            return;
        }
        Node currentNode = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            int index = c - 'a';
            if (index < 0 || index >= currentNode.getReferences().length) {
                // handle the invalid index
                return;
            }

            if (currentNode.getReferences()[index] == null) {
                currentNode.getReferences()[index] = new Node();
            }

            currentNode = currentNode.getReferences()[index];
            currentNode.setPrefixes(currentNode.getPrefixes() + 1);

            if (i == word.length() - 1) {
                currentNode.setTerm(new Term(word, weight));
                currentNode.setWords(currentNode.getWords() + 1);
            }
        }
        root.setPrefixes(root.getPrefixes() + 1);
    }

    @Override
    public Node buildTrie(String filename, int k) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length == 1) {
                    String word = parts[1].toLowerCase();
                    long weight = Long.parseLong(parts[0]);
                    addWord(word, weight);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    /**
     * @return k the the maximum number of suggestions that should be displayed
     */
    @Override
    public int numberSuggestions() {
        return 0;
    }

    @Override
    public Node getSubTrie(String prefix) {
        Node currentNode = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            int index = c - 'a';
            if (index < 0 || index >= currentNode.getReferences().length) {
                // handle the invalid index
                return null;
            }
            if (currentNode.getReferences()[index] == null) {
                return null;
            }
            currentNode = currentNode.getReferences()[index];
        }
        return currentNode;
    }

    @Override
    public int countPrefixes(String prefix) {
        Node subTrie = getSubTrie(prefix);
        return subTrie != null ? subTrie.getPrefixes() : 0;
    }

    @Override
    public List<ITerm> getSuggestions(String prefix) {
        List<ITerm> suggestions = new ArrayList<>();
        Node subTrie = getSubTrie(prefix);
        if (subTrie == null) {
            return suggestions;
        }
        collectTerms(subTrie, suggestions);
        return suggestions;
    }

    private void collectTerms(Node node, List<ITerm> terms) {
        if (node != null) {
            if (node.getWords() > 0) {
                terms.add(new Term(node.getTerm().getTerm(), node.getTerm().getWeight()));
            }
            for (Node child : node.getReferences()) {
                collectTerms(child, terms);
            }
        }
    }
}
