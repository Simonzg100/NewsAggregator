public class Term implements ITerm {

    private String term;
    private long weight;

    /**
     * Initialize a Term with a given query String and weight
     */
    public Term(String term, long weight) {
        if (term == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("weight cannot be negative");
        }
        this.term = term;
        this.weight = weight;
    }

    @Override
    public int compareTo(ITerm that) {
        return this.term.compareTo(that.getTerm());
    }

    @Override
    public String toString() {
        return weight + "\t" + term;
    }


    public long getWeight() {
        return this.weight;
    }

    @Override
    public String getTerm() {
        return this.term;
    }


    public void setWeight(long weight) {
        this.weight = weight;
    }

    public void setTerm(String term) {
    }

}
