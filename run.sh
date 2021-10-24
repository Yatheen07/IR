#Step 0: Delete the previously created indexes and result file
rm -r INDEX/
rm -r Results/

#Step 1: Clean and package the maven project
mvn clean package

#Step 2: Execute the Project
java -jar target/IR-Assignment-0.0.1.jar

#Step 3: Change to trec_eval directory and score the project. 
cd trec_eval

./trec_eval -m runid -m map -m P.5 -m gm_map ../cran/cranqrel ../Results/WhitespaceAnalyzer_results.txt
./trec_eval -m runid -m map -m P.5 -m gm_map ../cran/cranqrel ../Results/SimpleAnalyzer_results.txt
./trec_eval -m runid -m map -m P.5 -m gm_map ../cran/cranqrel ../Results/StandardAnalyzer_results.txt
./trec_eval -m runid -m map -m P.5 -m gm_map ../cran/cranqrel ../Results/EnglishAnalyzer_results.txt
