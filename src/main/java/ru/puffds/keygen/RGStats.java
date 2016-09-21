package ru.puffds.keygen;

import netscape.javascript.JSObject;
import ru.puffds.keygen.providers.*;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.math3.util.ArithmeticUtils;

/**
 * Вот тут функции проверки сгенерированных ключей по критериям
 */
public class RGStats {
    //Количество поставщиков ключей
    private final int PROVIDERS_COUNT;
    //Список наименований поставщиков ключей
    private String[] providerNames;
    //Поставщики ключей
    private KeyProvider[] providers;
    //Ключи, сгенерированные поставщиками
    private BitSet[][] keys;

    private int keysCount = 10;

    /**
     * Генерирует новые наборы ключей
     */
    private void reloadKeys() {
        for (int i = 0; i < PROVIDERS_COUNT; i++) {
            keys[i] = new BitSet[keysCount];
            for (int j = 0; j < keys[i].length; j++) {
                keys[i][j] = providers[i].getKey();
            }
        }
    }

    public RGStats() throws Exception {
        PROVIDERS_COUNT = 4;
        providers = new KeyProvider[PROVIDERS_COUNT];
        providers[0] = new AtmosphericNoizeProvider();
        providers[1] = new LinearCongruentProvider();
        providers[2] = new SecureRandomProvider();
        providers[3] = new BadKeyProvider();
        keys = new BitSet[PROVIDERS_COUNT][];
        providerNames = new String[PROVIDERS_COUNT];
        for (int i = 0; i < PROVIDERS_COUNT; i++) {
            providerNames[i] = providers[i].getName();
        }
        resetKeys(keysCount);
    }

    public int getProvidersCount() {
        return PROVIDERS_COUNT;
    }

    /**
     * Обновляет списки сгенерированных ключей для каждого из провайдеров
     *
     * @param count Количество ключей, которые необходимо сгенерировать
     */
    public void resetKeys(int count) {
        keysCount = count;
        reloadKeys();
        //TODO
    }

    /**
     * Запиливает наименования поставщиков ключей в массив JavaScript
     *
     * @param arr Массив, в который помещаются наименования
     * @return Исходный массив (в который уж помещены наименования)
     */
    public JSObject getProviderNames(JSObject arr) throws UnsupportedEncodingException {
        int count = Integer.parseInt(arr.getMember("length").toString());
        for (int i = 0; i < count; i++) {
            arr.setSlot(i, providerNames[i]);
        }
        return arr;
    }

    /**
     * Вычисляет коэффициенты Пирсона для набора ключей
     *
     * @param keys Набор ключей
     * @return
     */
    protected double[] calcPearson(BitSet[] keys) {
        int[] massive = new int[]{2, 4, 16};
        int[] massivetwo = new int[] {1, 2, 4};
        double[] average = new double[3];
        for (BitSet bs : keys) {
            int tempthree = 0;
            for (int i : massive) {
                tempthree++;
                int[] temp = new int[i];
                int keyLength = massivetwo[tempthree-1];
                for (int k = 0; k < 256/keyLength; k++) {
                    int temptwo = 0;
                    for (int j = 0; j < keyLength; j++) {
                        if (bs.get(k*keyLength+j)) {
                            temptwo = temptwo | (1 << (keyLength-1-j));
                        }
                    }
                    if (temp.length < temptwo + 1) {
                        int d = 8;
                    }
                    temp[temptwo]++;
                }
                double res = 0;
                int n = 256/(massivetwo[tempthree-1]*massive[tempthree-1]);
                for (int j : temp){
                    res += (Math.pow(j - n, 2))/n;
                }
                average[tempthree-1] += res/keys.length;
            }
        }
        return average;
    }

    /**
     * Вычисляет коэффициент Пирсона для ключей каждого из генераторов
     *
     * @return Массив с коэффициентом для каждого из генераторов в порядке их расположения
     */
    protected double[] calcPearsonArrays() {
        double[] res = new double[PROVIDERS_COUNT*3];
        for (int i = 0; i < PROVIDERS_COUNT; i++) {
            double[] tmpRes = calcPearson(keys[i]);
            for (int j = 0; j < 3; j++) {
                res[i*3+j] = tmpRes[j];
            }
        }
        return res;
    }

    /**
     * Запиливает результаты проверки ключей по Пирсону в массив JavaScript
     *
     * @param arr Массив, в который помещаются значения
     * @return Исходный массив
     */
    public JSObject getPearsonStats(JSObject arr) {
        return setJsArray(arr, calcPearsonArrays());
    }

    /**
     * Алгоритм Евклида. Находит наибольший делитель двух числел.
     * Юзается в вычислении коэффициента Чезаро.
     * @param p
     * @param q
     * @return
     */
    protected int gcd(int p, int q) {
        if (q == 0) return p;
        else return gcd(q, p % q);
    }

    /**
     * Вычисляет коэффициент Чезаро для набора ключей
     *
     * @param keys Набор ключей
     * @return
     */
    protected double calcChesaro(BitSet[] keys) {
        double res=0;
        int keyLength = 8;
        for (BitSet bs : keys) {
            int[] bts = new int[256/keyLength];
            for (int i = 0; i < 256/keyLength; i++) {
                bts[i]=0;
                for (int j = 0; j < keyLength; j++) {
                    if (bs.get(i*keyLength+j)) {
                        bts[i] = bts[i] | (1 << (keyLength-1-j));
                    }
                }
            }

            int k = 0;
            for (int i = 0; i < 256/keyLength; i++) {
                for (int j = i+1; j < 256/keyLength; j++) {
                    if (gcd (bts[i], bts[j]) == 1) k++;
                }
            }
            if (k==0) k = 1;
            double q = Math.abs(Math.PI - Math.sqrt((6*(256/keyLength)*(256/keyLength-1))/k));
            res += q/(double)(keys.length);
        }
        return res-1;
    }
    /**
     * Вычисляет коэффициент Чезаро для ключей каждого из генераторов
     *
     * @return Массив с коэффициентом для каждого из генераторов в порядке их расположения
     */
    protected double[] calcChesaroArray() {
        double[] res = new double[PROVIDERS_COUNT];
        for (int i = 0; i < PROVIDERS_COUNT; i++) {
            res[i] = calcChesaro(keys[i]);
        }
        return res;
    }

    /**
     * Запиливает результаты проверки ключей по критерию Чезаро в массив JavaScript
     *
     * @param arr Массив, в который помещаются значения
     * @return Исходный массив
     */
    public JSObject getChesaroStats(JSObject arr) {
        return setJsArray(arr, calcChesaroArray());
    }

    /**
     * Вычисляет коэффициент Критерия серий для набора ключей
     *
     * @param keys Набор ключей
     * @return
     */
    protected double calcSeries(BitSet[] keys) {
        double res=0;
        for (BitSet bs : keys) {
            res += bs.cardinality()/(256.*keys.length);
        }
        return Math.abs(res-0.5);
    }

    /**
     * Вычисляет коэффициент Критерия серий для ключей каждого из генераторов
     *
     * @return Массив с коэффициентом для каждого из генераторов в порядке их расположения
     */
    protected double[] calcSeriesArray() {
        double[] res = new double[PROVIDERS_COUNT];
        for (int i = 0; i < PROVIDERS_COUNT; i++) {
            res[i] = calcSeries(keys[i]);
        }
        return res;
    }

    /**
     * Запиливает результаты проверки ключей по Критерию серий в массив JavaScript
     *
     * @param arr Массив, в который помещаются значения
     * @return Исходный массив
     */
    public JSObject getSeriesStats(JSObject arr) {
        return setJsArray(arr, calcSeriesArray());
    }


    /**
     * Копирует содержимое массива Java в массив JavaScript
     *
     * @param arr  Массив, в который копируем
     * @param vals Массив-источник
     * @return Исходный массив JavaScript
     */
    public JSObject setJsArray(JSObject arr, double[] vals) {
        int count = Integer.parseInt(arr.getMember("length").toString());
        for (int i = 0; i < count; i++) {
            arr.setSlot(i, vals[i]);
        }
        return arr;
    }

}
