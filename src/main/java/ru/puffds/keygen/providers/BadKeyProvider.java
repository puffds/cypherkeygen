package ru.puffds.keygen.providers;

import java.util.BitSet;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Некриптостойкий генератор
 */
public class BadKeyProvider implements KeyProvider{
    private boolean badtrip = false;

    @Override
    public BitSet getKey() {
        BitSet res = new BitSet();
        badtrip = !badtrip;
        for (int i = 0; i < 256; i++) {
            badtrip = !badtrip;
            if (badtrip) res.set(i);
        }
        return res;
    }

    @Override
    public String getName() {
        return "Некриптостойкий генератор";
    }
}
