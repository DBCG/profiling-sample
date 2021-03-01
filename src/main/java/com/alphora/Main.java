package com.alphora;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;;

public class Main {
    private final static AtomicBoolean running = new AtomicBoolean(true);

    // This function just loops "doWork" until you press enter.
    // It's not important for the purposes of this example. "doWork"
    // is where we're going to start.
    public static void main(String[] args) {
        Thread thread = new Thread("work thread") {
            @Override
            public void run() {
                while (running.get()) {
                    doWork();
                }
            }
        };

        thread.start();

        Console c = System.console();
        c.printf("\nPress ENTER to close this application\n");
        c.readLine();
        running.set(false);

        try {
            thread.join();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    // This function just times how long it takes to do an iteration of the work.
    private static final void doWork() {
        long startTime = System.currentTimeMillis();

        int numLoops = 2;

        // Do each loop a couple times to get an average result
        for (int i = 0; i < numLoops; i++) {
            doWords();
        }

        long endTime = System.currentTimeMillis();
        long totalTime = (endTime - startTime);

        System.out.println("Total: " +  totalTime + " milliseconds Average: "+ totalTime / numLoops + " milliseconds");
    }

    // Load text file, count all words, write text file
    private static final void doWords() {
        List<String> words = getAllWords();
        Map<String, Integer> counts = countTotalCharactersForWord(words);
        writeCount(counts);
    }

    private static final Map<String, Integer> countTotalCharactersForWord(List<String> words) {
        Map<String, Integer> counts = new HashMap<>();

        counts = loopCountWorst(words);
        
        // loopCountBad(words);
        // streamCount(words);

        return counts;
    }

    private static final Map<String, Integer> streamCount(List<String> words) {
         return words.stream().collect(Collectors.groupingBy(x -> x, Collectors.summingInt(x -> x.length())));
    }

    private static final Map<String, Integer> loopCountWorst(List<String> words) {
        Map<String, Integer> counts = new HashMap<>();

        Map<String, String> concatenatedWords = new HashMap<>();
        for (String word : words) {
            if (concatenatedWords.containsKey(word)) {
                // Calculate how many times the word occurred by dividing the length of the entry
                // by the word size, then add one, then append the word that many times
                String oldWord = concatenatedWords.get(word);
                int count = oldWord.length() / word.length();
                count += 1;
                StringBuilder builder = new StringBuilder(word.length() * count);
                for (int i = 0; i < count; i++) {
                    builder.append(word);
                }

                concatenatedWords.put(word, builder.toString());
            }
            else {
                concatenatedWords.put(word, word);
            }
        }

        for (Map.Entry<String, String> word : concatenatedWords.entrySet()) {
            counts.put(word.getKey(), word.getValue().length());
        }

        return counts;
    }

    private static final Map<String, Integer> loopCountBad(List<String> words) {
        Map<String, Integer> counts = new HashMap<>();

        Map<String, String> concatenatedWords = new HashMap<>();
        for (String word : words) {
            if (concatenatedWords.containsKey(word)) {
                String oldWord = concatenatedWords.get(word);
                oldWord += word;
                concatenatedWords.put(word, oldWord);
            }
            else {
                concatenatedWords.put(word, word);
            }
        }

        for (Map.Entry<String, String> word : concatenatedWords.entrySet()) {
            counts.put(word.getKey(), word.getValue().length());
        }

        return counts;
    }

    private static final void writeCount(Map<String, Integer> counts) {
        try (FileOutputStream stream = new FileOutputStream("output.txt", false)) {
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            // BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                writer.write(entry.getKey());
                writer.write(":");
                writer.write(entry.getValue().toString());
                writer.write(System.lineSeparator());
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final List<String> getAllWords() {

        try (InputStream stream = Main.class.getResourceAsStream("sample.txt")) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            List<String> words = reader.lines().flatMap(x -> Stream.of(x.split(" "))).collect(Collectors.toList());
            return words;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
