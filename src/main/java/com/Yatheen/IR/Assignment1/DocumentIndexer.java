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

    /** Index all text files under a directory. */
    public void indexMethod() {
        String englishIndexPath = "INDEX/ENGLISH_ANALYZER";
        String standardIndexPath = "INDEX/STANDARD_ANALYZER";
        String docsPath = "cran/cran.all.1400";

        final Path docDir = Paths.get(docsPath);

        if (!Files.isReadable(docDir)) {
            System.out.println("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();
        try {

            Directory englishIndexDir = FSDirectory.open(Paths.get(englishIndexPath));
            Directory standardIndexDir = FSDirectory.open(Paths.get(standardIndexPath));

            //Analyzer analyzer = new SimpleAnalyzer();
            //Analyzer analyzer = new WhitespaceAnalyzer();
            Analyzer standardAnalyzer = new StandardAnalyzer();
            Analyzer englishAnalyser = new EnglishAnalyzer();

			IndexWriterConfig indexWriterConfigEnglish = new IndexWriterConfig(englishAnalyser);
            IndexWriterConfig indexWriterConfigStandard = new IndexWriterConfig(standardAnalyzer);

            //BM25 Similarity
            //iwc.setSimilarity(new BM25Similarity());

            //Classic Similarity
            //iwc.setSimilarity(new ClassicSimilarity());

            //LMDirichletSimilarity
            //iwc.setSimilarity(new LMDirichletSimilarity());

            //Trying a multi similarity model
            indexWriterConfigEnglish.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));
            indexWriterConfigStandard.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new ClassicSimilarity()}));

            //Trying another multi similarity model
            //iwc.setSimilarity(new MultiSimilarity(new Similarity[]{new BM25Similarity(),new LMDirichletSimilarity()}));

            //Trying another multi similarity model
            //iwc.setSimilarity(new MultiSimilarity(new Similarity[]{new ClassicSimilarity(),new LMDirichletSimilarity()}));

            indexWriterConfigEnglish.setOpenMode(OpenMode.CREATE);
            indexWriterConfigStandard.setOpenMode(OpenMode.CREATE);

            IndexWriter indexWriterEnglish = new IndexWriter(englishIndexDir, indexWriterConfigEnglish);
            IndexWriter indexWriterStandard = new IndexWriter(standardIndexDir, indexWriterConfigStandard);
            
            indexDoc(indexWriterEnglish, docDir);
            indexDoc(indexWriterStandard, docDir);

            //Using writer.forceMerge to maximise search performance.
            indexWriterEnglish.forceMerge(1);
            indexWriterStandard.forceMerge(1);

            indexWriterEnglish.close();
            indexWriterStandard.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    /** Indexes the 'cran.all.1400' file */
    static void indexDoc(IndexWriter writer, Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            Boolean first = true;
            System.out.println("Indexing documents.");

            String currentLine = bufferedReader.readLine();
            String fullText = "";
            System.out.println(currentLine);
            while(currentLine != null){
                Document doc = new Document();
                if(currentLine.startsWith(".I")){
                    /*
                     * I think the ID of the document does not make sense to be analysed,
                     * hence it is just directly stored without any analysis.
                     */
                    doc.add(new StringField("id", currentLine.substring(3), Field.Store.YES));
                    currentLine = bufferedReader.readLine();
                }
                if (currentLine.startsWith(".T")){
                    currentLine = bufferedReader.readLine();
                    while(!currentLine.startsWith(".A")){
                        fullText += currentLine + " ";
                        currentLine = bufferedReader.readLine();
                    }
                    doc.add(new TextField("title", fullText, Field.Store.YES));
                    fullText = "";
                }
                if (currentLine.startsWith(".A")){
                    currentLine = bufferedReader.readLine();
                    while(!currentLine.startsWith(".B")){
                        fullText += currentLine + " ";
                        currentLine = bufferedReader.readLine();
                    }
                    doc.add(new TextField("author", fullText, Field.Store.YES));
                    fullText = "";
                }
                if (currentLine.startsWith(".B")){
                    currentLine = bufferedReader.readLine();
                    while(!currentLine.startsWith(".W")){
                        fullText += currentLine + " ";
                        currentLine = bufferedReader.readLine();
                    }
                    /*
                     * After a bit of analysis, I found that for this dataset, analysing
                     * and storing bibliography details proved to be slightly inefficient.
                     */
                    doc.add(new StringField("bibliography", fullText, Field.Store.YES));
                    fullText = "";
                }
                if (currentLine.startsWith(".W")){
                    currentLine = bufferedReader.readLine();
                    while(currentLine != null && !currentLine.startsWith(".I")){
                        fullText += currentLine + " ";
                        currentLine = bufferedReader.readLine();
                    }
                    //Not storing the words in an attempt to save storage space.
                    doc.add(new TextField("words", fullText, Field.Store.NO));
                    fullText = "";
                }
                writer.addDocument(doc);
            }
        }
    }
}
