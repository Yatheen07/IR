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
	
    public void searchMethod() throws Exception {
    	
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
    		
    		
    		indexSearcher.setSimilarity(new ClassicSimilarity());
    		//indexSearcher.setSimilarity(new BM25Similarity());
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
                performSearch(indexSearcher, resultsWriter, Integer.parseInt(id), indexQuery,currentAnalyzer.toUpperCase());
            }
            
            resultsWriter.close();
            indexReader.close();
            Date endTime = new Date();
            System.out.println("Result generated in "+(endTime.getTime() - startTime.getTime()) +" milliseconds");
    		System.out.println("=========================================================");
    	}

//    	String englishIndexPath = "INDEX/ENGLISH_ANALYZER";
//        String standardIndexPath = "INDEX/STANDARD_ANALYZER";
//        
//
//        IndexReader englishReader = DirectoryReader.open(FSDirectory.open(Paths.get(englishIndexPath)));
//        IndexReader standardReader = DirectoryReader.open(FSDirectory.open(Paths.get(standardIndexPath)));
//        
//        IndexSearcher englishSearcher = new IndexSearcher(englishReader);
//        IndexSearcher standardSearcher = new IndexSearcher(standardReader);
//
//        //Analyzer analyzer = new SimpleAnalyzer();
//        //Analyzer analyzer = new WhitespaceAnalyzer();
//        Analyzer standardAnalyzer = new StandardAnalyzer();
//        Analyzer englishAnalyser = new EnglishAnalyzer();
//
//        String results_path_english = "results_english_analyzer.txt";
//        String results_path_standard = "results_standard_analyzer.txt";
//        
//        PrintWriter writerEnglish = new PrintWriter(results_path_english, "UTF-8");
//        PrintWriter writerStandard = new PrintWriter(results_path_standard, "UTF-8");
//
//        //BM25 Similarity
//        //searcher.setSimilarity(new BM25Similarity());
//
//        //Classic Similarity
//        //searcher.setSimilarity(new ClassicSimilarity());
//
//        //LMDirichletSimilarity
//        //searcher.setSimilarity(new LMDirichletSimilarity());
//
//        //Trying a multi similarity model
//        englishSearcher.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));
//        standardSearcher.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));
//
//        //Trying another multi similarity model
//        //searcher.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new LMDirichletSimilarity()}));
//
//        //Trying another multi similarity model
//        //searcher.setSimilarity(new MultiSimilarity(new Similarity[]{new ClassicSimilarity(),new LMDirichletSimilarity()}));
//
//        String queriesPath = "cran/cran.qry";
//        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(queriesPath), StandardCharsets.UTF_8);
//        MultiFieldQueryParser englishParser = new MultiFieldQueryParser(new String[]{"title", "words"}, englishAnalyser);
//        MultiFieldQueryParser standardParser = new MultiFieldQueryParser(new String[]{"title", "words"}, standardAnalyzer);
//
//        String currentLine = bufferedReader.readLine();
//
//        System.out.println("Reading in queries and creating search results.");
//
//        String id = "";
//        int i=0;
//
//        while (currentLine != null) {
//            i++;
//            if (currentLine.startsWith(".I")) {
//                id = Integer.toString(i);
//                currentLine = bufferedReader.readLine();
//            }
//            if (currentLine.startsWith(".W")) {
//                currentLine = bufferedReader.readLine();
//                while (currentLine != null && !currentLine.startsWith(".I")) {
//                    queryString += currentLine + " ";
//                    currentLine = bufferedReader.readLine();
//                }
//            }
//            queryString = queryString.trim();
//            Query englishQuery = englishParser.parse(QueryParser.escape(queryString));
//            Query standardQuery = standardParser.parse(QueryParser.escape(queryString));
//            queryString = "";
//            performSearch(englishSearcher, writerEnglish, Integer.parseInt(id), englishQuery);
//            performSearch(standardSearcher, writerStandard, Integer.parseInt(id), standardQuery);
//        }
//
//        System.out.println("Results have been written to the 'results.txt' file.");
//        writerEnglish.close();
//        writerStandard.close();
//        englishReader.close();
//        standardReader.close();
    }


    // Performs search and writes results to the writer
    public static void performSearch(IndexSearcher searcher, PrintWriter writer, Integer queryNumber, Query query, String title) throws IOException {
        TopDocs results = searcher.search(query, 999);
        ScoreDoc[] hits = results.scoreDocs;

        // To write the results for each hit in the format expected by the trec_eval tool.
        for (int i = 0; i < hits.length; i++) {
            Document doc = searcher.doc(hits[i].doc);
            writer.println(queryNumber + " Q0 " + doc.get("DocumentID") + " " + i + " " + hits[i].score + " "+ title);
        }
    }
}