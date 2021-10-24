package com.Yatheen.IR.Assignment1;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public void searchMethod() throws Exception {

    	String englishIndexPath = "INDEX/ENGLISH_ANALYZER";
        String standardIndexPath = "INDEX/STANDARD_ANALYZER";
        String queryString = "";

        IndexReader englishReader = DirectoryReader.open(FSDirectory.open(Paths.get(englishIndexPath)));
        IndexReader standardReader = DirectoryReader.open(FSDirectory.open(Paths.get(standardIndexPath)));
        
        IndexSearcher englishSearcher = new IndexSearcher(englishReader);
        IndexSearcher standardSearcher = new IndexSearcher(standardReader);

        //Analyzer analyzer = new SimpleAnalyzer();
        //Analyzer analyzer = new WhitespaceAnalyzer();
        Analyzer standardAnalyzer = new StandardAnalyzer();
        Analyzer englishAnalyser = new EnglishAnalyzer();

        String results_path_english = "results_english_analyzer.txt";
        String results_path_standard = "results_standard_analyzer.txt";
        
        PrintWriter writerEnglish = new PrintWriter(results_path_english, "UTF-8");
        PrintWriter writerStandard = new PrintWriter(results_path_standard, "UTF-8");

        //BM25 Similarity
        //searcher.setSimilarity(new BM25Similarity());

        //Classic Similarity
        //searcher.setSimilarity(new ClassicSimilarity());

        //LMDirichletSimilarity
        //searcher.setSimilarity(new LMDirichletSimilarity());

        //Trying a multi similarity model
        englishSearcher.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));
        standardSearcher.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));

        //Trying another multi similarity model
        //searcher.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new LMDirichletSimilarity()}));

        //Trying another multi similarity model
        //searcher.setSimilarity(new MultiSimilarity(new Similarity[]{new ClassicSimilarity(),new LMDirichletSimilarity()}));

        String queriesPath = "cran/cran.qry";
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(queriesPath), StandardCharsets.UTF_8);
        MultiFieldQueryParser englishParser = new MultiFieldQueryParser(new String[]{"title", "words"}, englishAnalyser);
        MultiFieldQueryParser standardParser = new MultiFieldQueryParser(new String[]{"title", "words"}, standardAnalyzer);

        String currentLine = bufferedReader.readLine();

        System.out.println("Reading in queries and creating search results.");

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
            Query englishQuery = englishParser.parse(QueryParser.escape(queryString));
            Query standardQuery = standardParser.parse(QueryParser.escape(queryString));
            queryString = "";
            performSearch(englishSearcher, writerEnglish, Integer.parseInt(id), englishQuery);
            performSearch(standardSearcher, writerStandard, Integer.parseInt(id), standardQuery);
        }

        System.out.println("Results have been written to the 'results.txt' file.");
        writerEnglish.close();
        writerStandard.close();
        englishReader.close();
        standardReader.close();
    }


    // Performs search and writes results to the writer
    public static void performSearch(IndexSearcher searcher, PrintWriter writer, Integer queryNumber, Query query) throws IOException {
        TopDocs results = searcher.search(query, 999);
        ScoreDoc[] hits = results.scoreDocs;

        // To write the results for each hit in the format expected by the trec_eval tool.
        for (int i = 0; i < hits.length; i++) {
            Document doc = searcher.doc(hits[i].doc);
            writer.println(queryNumber + " 0 " + doc.get("id") + " " + i + " " + hits[i].score + " STANDARD");
        }
    }
}