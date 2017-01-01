package org.circuit.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {

    private static LRUCache<Integer, Integer> getMaxCache = new LRUCache<Integer, Integer>(50);

    private static LRUCache<Pair<Integer, Integer>, Integer> pickCache = new LRUCache<Pair<Integer, Integer>, Integer>(50);

    public static int getMax(int size) {

        Integer cachedAnswer = null;
        if ((cachedAnswer = getMaxCache.get(size)) != null) {
            return cachedAnswer.intValue();
        }


        int answer = 0;
        for (int i = size; i >= 1 ; i--) {
            answer = answer + i;
        }

        getMaxCache.put(size, answer);
        return answer;
    }

    public static int pick(int random, int size) {

        Integer cachedAnswer = null;
        if ((cachedAnswer = pickCache.get(Pair.of(random, size))) != null) {
            return cachedAnswer.intValue();
        }


        int localSize = size;
        int r = random;
        int i = 0;

        while (r >= localSize) {
            r = r - localSize;
            localSize = localSize - 1;
            i++;

        }

        pickCache.put(Pair.of(random, size), Integer.valueOf(i));

        return i;
    }

    public static int raffle(int size) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return pick(random.nextInt(getMax(size)), size);
    }

    public static void main(String args[]) {


        long initial = System.currentTimeMillis();

        ThreadLocalRandom random = ThreadLocalRandom.current();

        Map<Integer, Integer> map = new TreeMap<Integer, Integer>();

        for (int i = 0; i < 1000000; i++) {

            int s = pick(random.nextInt(getMax(10000)), 10000);

            Integer total = map.get(s);

            if (total == null) {
                map.put(s, 1);
            }
            else {
                map.put(s, total + 1);
            }
        }


        for (Map.Entry<Integer, Integer>  entry : map.entrySet()) {
            System.out.println(String.format("%d %d", entry.getKey(), entry.getValue()));

        }

        System.out.println(String.format("Took: %d ms", (System.currentTimeMillis() - initial)));

    }


    public static class LRUCache<K, V> extends LinkedHashMap<K, V> {
		private static final long serialVersionUID = 1L;
		private int size;


        private LRUCache(int size) {
            super(size, 0.75f, true);
            this.size = size;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > size;
        }

        public static <K, V> LRUCache<K, V> newInstance(int size) {
            return new LRUCache<K, V>(size);
        }

    }

}
