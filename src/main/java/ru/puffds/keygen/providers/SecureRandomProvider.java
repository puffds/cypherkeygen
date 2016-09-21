package ru.puffds.keygen.providers;

import java.security.SecureRandom;
import java.util.BitSet;

/**
 * Генератор ключей на основе криптостойкого встроенного рандома java - SecureRandom
 */
public class SecureRandomProvider implements KeyProvider {
    SecureRandom rand;
    byte[] rBytes;

    public SecureRandomProvider() {
        rand = new SecureRandom();
        rBytes = new byte[256/8];
    }

    @Override
    public BitSet getKey() {
        rand.nextBytes(rBytes);
        return BitSet.valueOf(rBytes);
    }

    @Override
    public String getName() {
        return "Java SecureRandom ("+rand.getAlgorithm()+")";
    }
}
