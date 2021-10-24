package com.Yatheen.IR.Assignment1;

public class MainApplication {
	public static void main (String[] args) throws Exception {
        DocumentIndexer indexer = new DocumentIndexer();
        QueryHandler searcher = new QueryHandler();
        System.out.println();
        indexer.indexMethod();
        searcher.searchMethod();
        
        System.out.println("Evalutaing Scores: trec_eval");
    }
}
