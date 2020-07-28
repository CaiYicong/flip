package org.alibaba;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class WordGraph {
    private static final Pattern WORD_PATTERN = Pattern.compile("^[a-zA-Z]+$");
    private final HashMap<Integer, Map<String, List<String>>> graphNodes = new HashMap<>();
    private final StringBuilder sb = new StringBuilder();

    public void parseSource(String sourceWordsFile, String parsedWordsFile) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(sourceWordsFile))));
        String word;
        while ((word = br.readLine()) != null) {
            if (WORD_PATTERN.matcher(word).matches()) {
                addWord(word);
            }
        }
        outputToFile(parsedWordsFile);
    }

    public void loadParsed(String parsedWordsFile) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(parsedWordsFile))));
        String graphLine;
        while ((graphLine = br.readLine()) != null) {
            loadGraphWord(graphLine);
        }
    }

    private void outputToFile(String outputPath) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Map<String, List<String>>> entryForLength : graphNodes.entrySet()) {
            for (Map.Entry<String, List<String>> wordFlipEntry : entryForLength.getValue().entrySet()) {
                sb.append(entryForLength.getKey()).append(":").append(wordFlipEntry.getKey()).append(":");
                for (String flip : wordFlipEntry.getValue()) {
                    sb.append(flip).append(",");
                }
                sb.append("\n");
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write(sb.toString());
            writer.flush();
        }
    }

    private void addWord(String word) {
        if (!graphNodes.containsKey(word.length())) {
            graphNodes.put(word.length(), new HashMap<>());
        }

        Map<String, List<String>> graph = graphNodes.get(word.length());
        if (!graph.containsKey(word)) {
            graph.put(word, new ArrayList<>());
        }

        for (int i = 0; i < word.length(); i++) {
            for (char j = 'a'; j <= 'z'; j++) {
                if (word.charAt(i) != j) {
                    addWordEdge(word, i, j);
                }
            }
            for (char j = 'A'; j <= 'Z'; j++) {
                if (word.charAt(i) != j) {
                    addWordEdge(word, i, j);
                }
            }
        }
    }

    private void addWordEdge(String word, int index, char replaceChar) {
        sb.setLength(0);
        sb.append(word);
        sb.replace(index, index + 1, String.valueOf(replaceChar));
        Map<String, List<String>> graph = graphNodes.get(word.length());
        if (graph.containsKey(sb.toString())) {
            graph.get(sb.toString()).add(word);
            graph.get(word).add(sb.toString());
        }
    }

    private void loadGraphWord(String graphLine) {
        String[] graphParts = graphLine.split(":");
        Integer wordLength = Integer.valueOf(graphParts[0]);
        if (!graphNodes.containsKey(wordLength)) {
            graphNodes.put(wordLength, new HashMap<>());
        }
        List<String> flipList = new ArrayList<>();
        if (graphParts.length == 3) {
            flipList.addAll(Arrays.asList(graphParts[2].split(",")));
        }

        graphNodes.get(wordLength).put(graphParts[1], flipList);
    }

    public List<String> getFlipPath(String fromWord, String toWord) {
        if (fromWord == null || toWord == null ||
                !(graphNodes.containsKey(fromWord.length())
                        && graphNodes.get(fromWord.length()).containsKey(fromWord)
                        && graphNodes.get(toWord.length()).containsKey(toWord))) {
            return null;
        }

        return getCommonFlip(fromWord, toWord);
    }

    private List<String> getCommonFlip(String fromWord, String toWord) {
        Map<String, List<String>> graphForLength = graphNodes.get(fromWord.length());
        List<String> fromFlipList = graphForLength.get(fromWord);
        List<String> toFlipList = graphForLength.get(toWord);

        if (fromFlipList.contains(toWord)) {
            List<String> flipPath = new ArrayList<>();
            flipPath.add(fromWord);
            flipPath.add(toWord);
            return flipPath;
        }

        for (String fromFlip : fromFlipList) {
            if (toFlipList.contains(fromFlip)) {
                List<String> flipPath = new ArrayList<>();
                flipPath.add(fromWord);
                flipPath.add(fromFlip);
                flipPath.add(toWord);
                return flipPath;
            }
        }

        for (String fromFlip : fromFlipList) {
            for (String toFlip : toFlipList) {
                List<String> subFlipPath = getCommonFlip(fromFlip, toFlip);
                if (subFlipPath != null) {
                    List<String> flipPath = new ArrayList<>();
                    flipPath.add(fromWord);
                    flipPath.addAll(subFlipPath);
                    flipPath.add(toWord);
                    return flipPath;
                }

            }
        }

        return null;
    }
}
