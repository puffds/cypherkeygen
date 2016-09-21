package ru.puffds.keygen.providers;

import java.util.BitSet;

/**
 * Базовый интерфейс генератора ключей
 */
public interface KeyProvider {
    public BitSet getKey();
    public String getName();
}
