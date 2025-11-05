package task3;

import java.util.*;
import java.util.concurrent.*;

// сравнение производительности трёх способов подсчёта слов
public class PerformanceComparison {

    // вариант 1 — synchronizedMap
    static class SynchronizedMapCounter {
        private final Map<String, Integer> map = Collections.synchronizedMap(new HashMap<>());

        public void count(String word) {
            synchronized (map) { // синхронизация на объекте карты
                map.put(word, map.getOrDefault(word, 0) + 1);
            }
        }
    }

    // вариант 2 — ConcurrentHashMap
    static class ConcurrentMapCounter {
        private final ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

        public void count(String word) {
            // атомарное увеличение счётчика
            map.merge(word, 1, Integer::sum);
        }
    }

    // вариант 3 — обычный HashMap с synchronized методом
    static class ExplicitLockCounter {
        private final Map<String, Integer> map = new HashMap<>();

        public synchronized void count(String word) {
            map.put(word, map.getOrDefault(word, 0) + 1);
        }
    }

    // тест производительности
    private static long test(Runnable task) throws InterruptedException {
        long start = System.currentTimeMillis();
        task.run();
        return System.currentTimeMillis() - start;
    }

    public static void main(String[] args) throws InterruptedException {
        int threads = 10;
        int operations = 100_000;
        List<String> words = new ArrayList<>();
        for (int i = 0; i < 100; i++) words.add("word" + i);

        // тестируем три варианта
        long syncTime = test(() -> runTest(new SynchronizedMapCounter(), threads, operations, words));
        long concTime = test(() -> runTest(new ConcurrentMapCounter(), threads, operations, words));
        long lockTime = test(() -> runTest(new ExplicitLockCounter(), threads, operations, words));

        // выводим результаты
        System.out.println("SynchronizedMap: " + syncTime + " ms");
        System.out.println("ConcurrentHashMap: " + concTime + " ms");
        System.out.println("ExplicitLock: " + lockTime + " ms");

        // показываем победителя
        long min = Math.min(syncTime, Math.min(concTime, lockTime));
        if (min == concTime) System.out.println("Winner: ConcurrentHashMap 🚀");
        else if (min == syncTime) System.out.println("Winner: SynchronizedMap");
        else System.out.println("Winner: ExplicitLock");
    }

    private static void runTest(Object counter, int threads, int operations, List<String> words) {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        Random random = new Random();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < operations; j++) {
                    String w = words.get(random.nextInt(words.size()));
                    if (counter instanceof SynchronizedMapCounter)
                        ((SynchronizedMapCounter) counter).count(w);
                    else if (counter instanceof ConcurrentMapCounter)
                        ((ConcurrentMapCounter) counter).count(w);
                    else
                        ((ExplicitLockCounter) counter).count(w);
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
