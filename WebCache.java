package task1;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class WebCache {

    // кеш страниц
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    // счётчик обращений
    private final ConcurrentHashMap<String, Integer> accessCount = new ConcurrentHashMap<>();

    public String get(String url) {
        // увеличиваем количество обращений
        accessCount.merge(url, 1, Integer::sum);
        return cache.get(url);
    }

    public void put(String url, String content) {
        // добавляем или обновляем страницу в кеше
        cache.put(url, content);
    }

    public int getAccessCount(String url) {
        return accessCount.getOrDefault(url, 0);
    }

    public Map<String, Integer> getTopAccessed(int n) {
        // сортируем по популярности и возвращаем топ N
        return accessCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    public static void main(String[] args) throws InterruptedException {
        WebCache cache = new WebCache();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // добавляем данные
        for (int i = 0; i < 100; i++) {
            int urlId = i;
            executor.submit(() -> {
                String url = "https://example.com/page" + (urlId % 20);
                cache.put(url, "Content of page " + urlId);
            });
        }

        // читаем данные
        for (int i = 0; i < 1000; i++) {
            int urlId = i;
            executor.submit(() -> {
                String url = "https://example.com/page" + (urlId % 20);
                cache.get(url);
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        // выводим топ 5
        System.out.println("Топ-5 самых популярных страниц:");
        cache.getTopAccessed(5).forEach((url, count) ->
                System.out.println(url + ": " + count + " обращений"));
    }
}
