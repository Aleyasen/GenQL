/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genql;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

/**
 *
 * @author amir
 */
public class MainApp {

    public static void main(String[] args) {
        MainApp m = new MainApp();
        m.dirNames.add("data/sample");
        m.index();
        m.search("obama");
        boolean[] feedback = new boolean[]{false, true, false};
        m.relevanceFeedbackSearch(feedback);
        feedback = new boolean[]{false, false, true};
        m.relevanceFeedbackSearch(feedback);
    }
    /**
     * The indexer creating the search index.
     */
    Indexer indexer = new Indexer();

    /**
     * The query posed by the user, used in search() and
     * relevanceFeedbackSearch()
     */
    private Query query;

    /**
     * The returned documents, used in search() and relevanceFeedbackSearch()
     */
    private PostingsList results;

    /**
     * Directories that should be indexed.
     */
    LinkedList<String> dirNames = new LinkedList<String>();

    /**
     * The query type (either intersection, phrase, or ranked).
     */
    int queryType = Index.RANKED_QUERY;

    /**
     * The ranking type (tf-idf).
     */
    int rankingType = Index.TF_IDF;

    /**
     * The word structure type (either unigram, bigram, or subphrase).
     */
    int structureType = Index.UNIGRAM;

    /**
     * Lock to prevent simultaneous access to the index.
     */
    Object indexLock = new Object();

    public void search(String querytext) {
        String queryString = SimpleTokenizer.normalize(querytext);
        query = new Query(queryString);
        // Search and print results. Access to the index is synchronized since
        // we don't want to search at the same time we're indexing new files
        // (this might corrupt the index).
        synchronized (indexLock) {
            if (structureType == genql.Index.BIGRAM) {
                results = indexer.bigram_index.search(query, queryType, rankingType, structureType);
            } else {
                results = indexer.index.search(query, queryType, rankingType, structureType);
            }
        }
        StringBuffer buf = new StringBuffer();
        if (results != null) {
            buf.append("Found " + results.size() + " matching document(s)\n\n");
            for (int i = 0; i < results.size(); i++) {
                buf.append(" " + i + ". ");
                String filename = indexer.index.docIDs.get("" + results.get(i).docID);
                if (filename == null) {
                    buf.append("" + results.get(i).docID);
                } else {
                    buf.append(filename);
                }
                if (queryType == Index.RANKED_QUERY) {
                    buf.append("   " + String.format("%.5f", results.get(i).score));
                }
                buf.append("\n");
            }
        } else {
            buf.append("Found 0 matching document(s)\n\n");
        }
        System.out.println(buf);
    }

    public void relevanceFeedbackSearch(boolean[] docIsRelevant) {
        // Check that a ranked search has been made prior to the relevance feedback
        StringBuffer buf = new StringBuffer();
        if ((results != null) && (queryType == Index.RANKED_QUERY)) {

            // Expand the current search query with the documents marked as relevant
            query.relevanceFeedback(results, docIsRelevant, indexer);

            // Perform a new search with the weighted and expanded query. Access to the index is
            // synchronized since we don't want to search at the same time we're indexing new files
            // (this might corrupt the index).
            synchronized (indexLock) {
                if (structureType == genql.Index.BIGRAM) {
                    results = indexer.bigram_index.search(query, queryType, rankingType, structureType);
                } else {
                    results = indexer.index.search(query, queryType, rankingType, structureType);
                }
            }
            buf.append("Search after relevance feedback:\n");
            buf.append("Found " + results.size() + " matching document(s)\n\n");
            for (int i = 0; i < results.size(); i++) {
                buf.append(" " + i + ". ");
                String filename = indexer.index.docIDs.get("" + results.get(i).docID);
                if (filename == null) {
                    buf.append("" + results.get(i).docID);
                } else {
                    buf.append(filename);
                }
                buf.append("   " + String.format("%.5f", results.get(i).score) + "\n");
            }
        } else {
            buf.append("There was no returned ranked list to give feedback on.\n\n");
        }
        System.out.println(buf);
    }

    /* ----------------------------------------------- */
    /**
     * Calls the indexer to index the chosen directory structure. Access to the
     * index is synchronized since we don't want to search at the same time
     * we're indexing new files (this might corrupt the index).
     */
    private void index() {
        synchronized (indexLock) {
            System.out.println("Importing index, please wait...");
            if (!indexer.index.importIndex()) {
                System.out.println("Indexing, please wait...");
                System.out.println(dirNames);
                for (int i = 0; i < dirNames.size(); i++) {
                    File dokDir = new File(dirNames.get(i));
                    indexer.processFiles(dokDir);
                }
                System.out.println("Saving popular entries, please wait...");
                indexer.sync_indexes();
                indexer.index.indexingOver();
                indexer.bigram_index.indexingOver();
            }
            System.out.println("Done!");
        }
    }
}
