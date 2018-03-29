package com.coenni;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NaiveBayesianExample {

    static Map<String, Long> azDigramFrequencies;
    static Map<String, Long> enDigramFrequencies;

    @Test
    public void detectLangTest() throws URISyntaxException, IOException {
        List<String> azTrainData = FileUtils.getContentFromFile("az.train.data");
        List<String> enTrainData = FileUtils.getContentFromFile("en.train.data");
        Double azTrainLinesCount = Double.valueOf(azTrainData.size());
        Double enTrainLinesCount = Double.valueOf(enTrainData.size());
        Double allTrainLinesCount = azTrainLinesCount + enTrainLinesCount;
        azDigramFrequencies = getDigramFrequencies(azTrainData);
        enDigramFrequencies = getDigramFrequencies(enTrainData);

        Double azDigramFrequencySum = 0d;
        Double enDigramFrequencySum = 0d;
        for (Map.Entry<String, Long> entry : azDigramFrequencies.entrySet()) {
           azDigramFrequencySum += entry.getValue();
        }
        for (Map.Entry<String, Long> entry : enDigramFrequencies.entrySet()) {
            enDigramFrequencySum += entry.getValue();
        }

        List<String> azGuessData = FileUtils.getContentFromFile("az.guess.data");
        List<String> enGuessData = FileUtils.getContentFromFile("en.guess.data");

        for (String line : azGuessData) {
            System.out.println("\n"+line);
            System.out.println(getPrediction(azTrainLinesCount, enTrainLinesCount, allTrainLinesCount, azDigramFrequencySum, enDigramFrequencySum, line));
        }

        for (String line : enGuessData) {
            System.out.println("\n"+line);
            System.out.println( getPrediction(azTrainLinesCount, enTrainLinesCount, allTrainLinesCount, azDigramFrequencySum, enDigramFrequencySum, line));
        }
    }

    private String getPrediction(Double azTrainLinesCount, Double enTrainLinesCount, Double allTrainLinesCount, Double azDigramFrequencySum, Double enDigramFrequencySum, String line) {
        List<String> guessLines = new ArrayList<String>();
        guessLines.add(line);
        Map<String, Long> digramFrequenciesOfLine = getDigramFrequencies(guessLines);

        Double azProbCond = (azTrainLinesCount/allTrainLinesCount);
        Double enProbCond = (enTrainLinesCount/allTrainLinesCount);
        for (Map.Entry<String, Long> entry : digramFrequenciesOfLine.entrySet()) {
            String digram =  entry.getKey();
            Long frequency = entry.getValue();
            azProbCond *= (1 + (azDigramFrequencies.get(digram)==null?0:azDigramFrequencies.get(digram)))/(azDigramFrequencies.size()+azDigramFrequencySum);
            enProbCond *= (1 + (enDigramFrequencies.get(digram)==null?0:enDigramFrequencies.get(digram)))/(enDigramFrequencies.size()+enDigramFrequencySum);
        }
        System.out.println(azProbCond);
        System.out.println(enProbCond);
        return getPredictionLang(azProbCond/enProbCond);
    }

    private String getPredictionLang(double predictionValue) {
        if(predictionValue>2)
            return Lang.AZ.name();
        if(predictionValue<0.5)
            return Lang.EN.name();
        return Lang.UNPREDICTED.name();
    }


    private Map<String, Long> getDigramFrequencies(List<String> trainData) {
        List<Map<String, Long>> digramFrequencies = new ArrayList<Map<String, Long>>();
        trainData.forEach(articleContent->{
            Map<String, Long> digramFrequenciesPerLine = Arrays
                .stream(articleContent.replaceAll("\\."," ").replaceAll("\\n"," ")
                    .replaceAll("(?<!^| ).(?! |$)", "$0$0") // double letters
                    .split("(?<=\\G.{2})")) // split into digrams
                .filter(s -> s.trim().length() >1) // discard short terms
                .filter(s->s.chars().allMatch(Character::isLetter))
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
            digramFrequencies.add(digramFrequenciesPerLine);
        });

        return digramFrequencies.stream().reduce((map1, map2) -> {
            Map<String, Long> result = new HashMap<String, Long>();
            map1.keySet().stream().forEach(key->result.put(key, map1.get(key)));
            map2.keySet().stream().forEach(mapKey->{
                if(result.containsKey(mapKey)){
                    result.put(mapKey, result.get(mapKey)+map2.get(mapKey));
                } else{
                    result.put(mapKey, map2.get(mapKey));
                }
            });
            return result;
        }).get();
    }



}
