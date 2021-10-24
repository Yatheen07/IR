mvn clean package

java -jar target/Lucene-Example-0.0.1-SNAPSHOT.jar

cd trec_eval

./trec_eval -m runid -m map -m P.5 -m gm_map ../cran/cranqrel ../results_english_analyzer.txt
./trec_eval -m runid -m map -m P.5 -m gm_map ../cran/cranqrel ../results_standard_analyzer.txt