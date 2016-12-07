

package genql;

import java.util.*;

public class Query {

    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();
    public genql.Vector vector = null;

    //Rocchio's algorithm parameters
    private static final double ALPHA = 0.5;
    private static final double BETA = 0.5;
    // We use gamma = 0 since no documents are marked as non relevant (positive feedback only)

    /**
     *  Creates a new empty Query
     */
    public Query() {
    }

    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
	StringTokenizer tok = new StringTokenizer( queryString );
	while ( tok.hasMoreTokens() ) {
	    terms.add( tok.nextToken() );
	    weights.add( new Double(1) );
	}
    }

    /**
     *  Returns the number of terms
     */
    public int size() {
	return terms.size();
    }

    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
	Query queryCopy = new Query();
	queryCopy.terms = (LinkedList<String>) terms.clone();
	queryCopy.weights = (LinkedList<Double>) weights.clone();
	return queryCopy;
    }

    /**
     *  Expands the Query using Relevance Feedback given for a ranked search.
     *  The expanded query vector is used to process a new search with weights updated to match
     *  the user preferences (Rocchio's algorithm)
     */
    public void relevanceFeedback( genql.PostingsList results, boolean[] docIsRelevant, genql.Indexer indexer ) {
	    // results contain the ranked list from the current search
	    // docIsRelevant contains the users feedback on which of the 10 first hits are relevant
        if(!(indexer.index instanceof genql.HashedIndex)) {
            return;
        }

        int relevant = 0;
        for(boolean b : docIsRelevant) {
            if(b) {
                ++relevant;
            }
        }

        int i = 0;
        int ndocs = indexer.index.docLengths.size();
        double score, docNorm, docLength;
        HashMap<String, Double> query = new HashMap<String, Double>();
        // Iterate through the retrieved documents
        for(genql.PostingsEntry pe : results.postingsEntries) {
            if(i >= docIsRelevant.length) {
                break;
            }
            // Filter the irrelevant documents
            if(docIsRelevant[i]) {
                //docLength = (double) indexer.index.docLengths.get("" + pe.docID);
                //docNorm = indexer.index.docNorm.get(pe.docID);
                // Get the words contained by the current document and the number of times they appear
                HashMap<String, Integer> words = ((genql.HashedIndex)indexer.index).uninverted_index.get(pe.docID);

                // For each word
                for(Map.Entry<String, Integer> word : words.entrySet()) {
                    Double weight = query.get(word.getKey());
                    // TF-IDF computation weighted by BETA and the number of relevant documents
                    score = ((genql.HashedIndex)indexer.index).getTfIdfScore(word.getKey(), pe.docID);
                    //score = (word.getValue() / (docLength * docNorm)) * Math.log(ndocs / (double) ((src.HashedIndex)indexer.index).index.get(word.getKey()).postingsEntries.size());
                    Double update = (BETA * score) / relevant;
                    if(weight == null) {
                        query.put(word.getKey(), update);
                    } else {
                        query.put(word.getKey(), weight + update);
                    }
                }
            }
            ++i;
        }
        for(String term : this.terms) {
            Double weight = query.get(term);
            Double update = ALPHA; // / this.terms.size();
            if(weight == null) {
                query.put(term, update);
            } else {
                query.put(term, weight + update);
            }
        }

        this.terms = new LinkedList(query.keySet());
        double[] array = new double[this.terms.size()];
        i = 0;
        for(String token : this.terms) {
            array[i] = query.get(token);
            ++i;
        }
        this.vector = new genql.Vector(array);
        this.vector.normalize();
    }
}
