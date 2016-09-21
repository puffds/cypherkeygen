package ru.puffds.keygen.providers;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Псевдослучайные числа с random.org
 * https://www.random.org/integers/
 */
public class AtmosphericNoizeProvider implements KeyProvider {
    final static private int ENTRIES_COUNT = 1500;
    protected BitSet[] entries;
    private int currentIndex;

    protected BitSet parseBitSet(String s) {
        BitSet res = new BitSet();
        byte[] bytes = s.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i]=='1') {
                res.set(i);
            }
            else if (bytes[i]!='0') {
                throw new IllegalArgumentException("Character "+bytes[i]+" cannot be writen to BitSet");
            }
        }
        return res;
    }

    public AtmosphericNoizeProvider() throws IOException, URISyntaxException {
        entries = new BitSet[ENTRIES_COUNT];
        Scanner sc = new Scanner(getClass().getClassLoader().getResourceAsStream("keys/atmospheric-noize.txt"));
        try {
            for (int i = 0; i < ENTRIES_COUNT && sc.hasNext(); i++) {
                entries[i] = parseBitSet(sc.next());
            }
        }
        finally {
            sc.close();
        }
        currentIndex = ThreadLocalRandom.current().nextInt(ENTRIES_COUNT);
    }

    public BitSet getKey() {
        currentIndex = (currentIndex+1)%ENTRIES_COUNT;
        return entries[currentIndex];
    }

    @Override
    public String getName() {
        return "Атмосферный шум (random.org)";
    }
}
