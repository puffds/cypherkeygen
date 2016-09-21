package ru.puffds.keygen.providers;

import java.util.BitSet;
import java.util.Random;

/**
 * Генератор на основе стандартного рандома Java
 * Рандом Java использует линейный конгруэнтный метод
 */
public class LinearCongruentProvider implements KeyProvider {

    private Random rand;
    private long[] rNums;

    public LinearCongruentProvider() {
        rand = new Random(System.nanoTime());
        rNums = new long[256/Long.SIZE];
    }

    @Override
    public BitSet getKey() {
        for (int i = 0; i < rNums.length; i++) {
            rNums[i] = rand.nextLong();
        }
        return BitSet.valueOf(rNums);
    }

    @Override
    public String getName() {
        return "Линейный конгруэнтный метод";
    }
}
