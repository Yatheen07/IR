package com.Yatheen.IR.Assignment1;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.io.PrintWriter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.FSDirectory;

/** Simple command-line based search demo. */
public class QueryHandler {
	private String RESULTS_DIRECTORY = "Results/";
	private String QUERY_FILE_PATH = "cran/cran.qry";
	
	QueryHandler(){
		File resultDir = new File(RESULTS_DIRECTORY);
		if(!resultDir.exists()) {
			resultDir.mkdirs();
		}
	}
	
    public void queryIndex() throws Exception {
    	
    	HashMap<String,Analyzer> analyzers = DocumentIndexer.getAnalyzers();
    	String[] INDEX_PATHS = DocumentIndexer.getINDEX_PATHS();
    	
    	int count = 0;
    	for(String currentAnalyzer: analyzers.keySet()) {
    		System.out.println("Started Querying the index using: "+currentAnalyzer);
    		Date startTime = new Date();
    		String queryString = "";
    		
    		Analyzer analyzer = analyzers.get(currentAnalyzer);
    		
    		String indexPath = INDEX_PATHS[count++];
    		
    		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
    		
    		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
    		
    		
    		//indexSearcher.setSimilarity(new ClassicSimilarity());
    		indexSearcher.setSimilarity(new BM25Similarity());
    		//indexSearcher.setSimilarity(new LMDirichletSimilarity());
    		//indexSearcher.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));
    		
    		String resultsPath = RESULTS_DIRECTORY+ currentAnalyzer + "_results.txt";
    		
    		PrintWriter resultsWriter = new PrintWriter(resultsPath, "UTF-8");
    		
    		BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(QUERY_FILE_PATH), StandardCharsets.UTF_8);
    		
    		MultiFieldQueryParser indexParser = new MultiFieldQueryParser(new String[]{"DocumentTitle", "DocumentWords"}, analyzer);
    		
    		String currentLine = bufferedReader.readLine();

            String id = "";
            int i=0;

            while (currentLine != null) {
                i++;
                if (currentLine.startsWith(".I")) {
                    id = Integer.toString(i);
                    currentLine = bufferedReader.readLine();
                }
                if (currentLine.startsWith(".W")) {
                    currentLine = bufferedReader.readLine();
                    while (currentLine != null && !currentLine.startsWith(".I")) {
                        queryString += currentLine + " ";
                        currentLine = bufferedReader.readLine();
                    }
                }
                queryString = queryString.trim();
                Query indexQuery = indexParser.parse(QueryParser.escape(queryString));
                queryString = "";
                searchIndex(indexSearcher, resultsWriter, Integer.parseInt(id), indexQuery,currentAnalyzer.toUpperCase());
            }
            
            resultsWriter.close();
            indexReader.close();
            Date endTime = new Date();
            System.out.println("Result generated in "+(endTime.getTime() - startTime.getTime()) +" milliseconds");
    		System.out.println("=========================================================");
    	}
    }


    public static void searchIndex(IndexSearcher searcher, PrintWriter writer, Integer queryNumber, Query query, String title) throws IOException {
        TopDocs results = searcher.search(query, 999);
        ScoreDoc[] hits = results.scoreDocs;

        // To write the results for each hit in the format expected by the trec_eval tool.
        for (int i = 0; i < hits.length; i++) {
            Document doc = searcher.doc(hits[i].doc);
            writer.println(queryNumber + " Q0 " + doc.get("DocumentID") + " " + i + " " + hits[i].score + " "+ title);
        }
    }
}