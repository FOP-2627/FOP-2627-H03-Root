package h03;

import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

/**
 * This class serves as a container for the methods that are to be implemented by the students for exercise H3.1.2.
 * <p>
 * It represents a "text search" problem. As a common task for natural language processing and document retrieval
 * (what google does), one wants to find out which documents are related to or relevant for a specific word (token).
 * <p>
 * It uses the following data structure:
 * <ul>
 *   <li>A <strong>token</strong> is a single word (String)</li>
 *   <li>A <strong>document</strong> is a 2D array of Strings: {@code String[][]} representing multiple sentences (rows) with words (columns)</li>
 *   <li>A <strong>corpus</strong> is a 3D array of Strings: {@code String[][][]} representing multiple documents</li>
 * </ul>
 * Example: A document with 2 sentences and 3 words each would be: {@code String[2][3]}
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public class TextSearch {

    /**
     * Prevent instantiation of this utility class.
     */
    private TextSearch() {
        throw new IllegalStateException("This class is not meant to be instantiated.");
    }

    /**
     * Checks if a word (token) appears anywhere in a document.
     * <p>
     * This method searches through all sentences (rows) and all words (columns) in the document
     * to find an occurrence of the given token.
     *
     * @param document a 2D array of Strings representing multiple sentences of words (e.g., {@code String[sentences][words]})
     * @param token    the word to search for
     * @return {@code true} if the token is found anywhere in the document; {@code false} otherwise
     */
    @StudentImplementationRequired("H3.1.2")
    public static boolean anyMatch(final String[][] document, final String token) {
        for (int row = 0; row < document.length; row++) {
            for (int tokenPos = 0; tokenPos < document[row].length; tokenPos++) {
                if (document[row][tokenPos].equals(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Counts how many documents in a corpus contain a specific word (token).
     * <p>
     * A document is considered "relevant" if the token appears anywhere in that document.
     * This method iterates through all documents in the corpus and uses {@code anyMatch()}
     * to check each one, counting how many documents are relevant.
     * <p>
     * Example: If searching for "the" in a corpus of 5 documents, and "the" appears in 3 of them,
     * this method returns 3 (not the total count of "the", but the count of documents containing it).
     *
     * @param corpus a 3D array of Strings representing multiple documents (e.g., {@code String[documents][sentences][words]})
     * @param token  the word to search for across all documents
     * @return the number of documents in the corpus that contain the token at least once
     */
    @StudentImplementationRequired("H3.1.2")
    public static int countRelevant(final String[][][] corpus, final String token) {
        int sum = 0;
        for (int documentId = 0; documentId < corpus.length; documentId++) {
            String[][] document = corpus[documentId];
            if (anyMatch(document, token)) sum++;
        }
        return sum;
    }
}
