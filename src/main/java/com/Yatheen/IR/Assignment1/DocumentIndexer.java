package com.Yatheen.IR.Assignment1;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class DocumentIndexer {

    private static HashMap<String,Analyzer> analyzers;
	private static String[] INDEX_PATHS;
	private static String datasetPath = "cran/cran.all.1400";
	
	DocumentIndexer(){
		analyzers = new HashMap<String,Analyzer>();
		analyzers.put("WhitespaceAnalyzer",new WhitespaceAnalyzer());
		analyzers.put("SimpleAnalyzer",new SimpleAnalyzer());
		analyzers.put("StandardAnalyzer",new StandardAnalyzer());
		analyzers.put("EnglishAnalyzer",new EnglishAnalyzer());
		
		INDEX_PATHS  = new String[]{
				"INDEX/WHITESPACE_ANALYZER",
				"INDEX/SIMPLE_ANALYZER",
				"INDEX/STANDARD_ANALYZER",
				"INDEX/ENGLISH_ANALYZER"
		};
	}
	
	public static HashMap<String, Analyzer> getAnalyzers() {
		return analyzers;
	}

	public static String[] getINDEX_PATHS() {
		return INDEX_PATHS;
	}
	
	private static Path datasetDirectory = Paths.get(datasetPath);
	
    public void indexMethod() {

        if (!Files.isReadable(datasetDirectory)) {
            System.out.println("Document directory '" + datasetDirectory.toAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        
        try {
        	int counter = 0;
        	for(String currentAnalyzer: analyzers.keySet()) {
        		System.out.println("Indexing the corpus using: "+currentAnalyzer);
        		Date startTime = new Date();
        		
        		Directory indexDir = FSDirectory.open(Paths.get(INDEX_PATHS[counter++]));
        		
        		Analyzer analyzer = analyzers.get(currentAnalyzer);
        		
        		IndexWriterConfig indexConfig = new IndexWriterConfig(analyzer);
        		
        		//indexConfig.setSimilarity(new LMDirichletSimilarity());
        		//indexConfig.setSimilarity(new ClassicSimilarity());
        		indexConfig.setSimilarity(new BM25Similarity());
        		//indexConfig.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));
        		
        		indexConfig.setOpenMode(OpenMode.CREATE);
        		
        		IndexWriter indexWriter = new IndexWriter(indexDir, indexConfig);
        		
        		indexData(indexWriter, datasetDirectory);
        		
        		indexWriter.forceMerge(1);
        		
        		indexWriter.close();
        		
        		Date endTime = new Date();
        		System.out.println("Completed Indexing in "+(endTime.getTime() - startTime.getTime()) +" milliseconds");
        		System.out.println("=========================================================");
        	}

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    private static void indexData(IndexWriter writer, Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

            String currentLine = bufferedReader.readLine();
            String fullText = "";
            while(currentLine != null){
                Document doc = new Document();
                if(currentLine.startsWith(".I")){
                    doc.add(new StringField("DocumentID", currentLine.substring(3), Field.Store.YES));
                    currentLine = bufferedReader.readLine();
                }
                if (currentLine.startsWith(".T")){
                    currentLine = bufferedReader.readLine();
                    while(!currentLine.startsWith(".A")){
                        fullText += currentLine + " ";
                        currentLine = bufferedReader.readLine();
                    }
                    doc.add(new TextField("DocumentTitle", fullText, Field.Store.YES));
                    fullText = "";
                }
                if (currentLine.startsWith(".A")){
                    currentLine = bufferedReader.readLine();
                    while(!currentLine.startsWith(".B")){
                        fullText += currentLine + " ";
                        currentLine = bufferedReader.readLine();
                    }
                    doc.add(new TextField("DocumentAuthour", fullText, Field.Store.YES));
                    fullText = "";
                }
                if (currentLine.startsWith(".B")){
                    currentLine = bufferedReader.readLine();
                    while(!currentLine.startsWith(".W")){
                        fullText += currentLine + " ";
                        currentLine = bufferedReader.readLine();
                    }
                    doc.add(new StringField("DocumentBibliography", fullText, Field.Store.YES));
                    fullText = "";
                }
                if (currentLine.startsWith(".W")){
                    currentLine = bufferedReader.readLine();
                    while(currentLine != null && !currentLine.startsWith(".I")){
                        fullText += currentLine + " ";
                        currentLine = bufferedReader.readLine();
                    }
                    doc.add(new TextField("DocumentWords", fullText, Field.Store.YES));
                    fullText = "";
                }
                writer.addDocument(doc);
            }
        }
    }
}
