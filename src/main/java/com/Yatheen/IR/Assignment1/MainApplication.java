package com.Yatheen.IR.Assignment1;

public class MainApplication {
	public static void main (String[] args) throws Exception {
        DocumentIndexer indexer = new DocumentIndexer();
        QueryHandler searcher = new QueryHandler();

        indexer.indexMethod();
        searcher.searchMethod();
    }
}
