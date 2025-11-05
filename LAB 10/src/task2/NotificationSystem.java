package task2;

import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationSystem {

    // список подписчиков
    private final CopyOnWriteArrayList<Subscriber> subscribers = new CopyOnWriteArrayList<>();

    public void subscribe(Subscriber subscriber) {
        // добавляем нового подписчика
        subscribers.add(subscriber);
    }

    public void unsubscribe(Subscriber subscriber) {
        // удаляем подписчика
        subscribers.remove(subscriber);
    }

    public void notifyAll(String message) {
        // рассылаем уведомление всем
        for (Subscriber s : subscribers) {
            s.onNotify(message);
        }
    }

    public int getSubscriberCount() {
        return subscribers.size();
    }

    public static void main(String[] args) throws InterruptedException {
        NotificationSystem system = new NotificationSystem();

        // поток 1 — добавляет подписчиков
        Thread addThread = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                system.subscribe(new EmailSubscriber("user" + i + "@example.com"));
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        // поток 2 — рассылает уведомления
        Thread notifyThread = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                system.notifyAll("Уведомление #" + i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        // поток 3 — удаляет некоторых подписчиков
        Thread removeThread = new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // удаляем каждого 10-го подписчика
            for (int i = 0; i < system.getSubscriberCount(); i += 10) {
                if (i < system.getSubscriberCount()) {
                    system.unsubscribe(system.subscribers.get(i));
                }
            }
        });

        // запускаем все потоки
        addThread.start();
        notifyThread.start();
        removeThread.start();

        addThread.join();
        notifyThread.join();
        removeThread.join();

        // выводим итог
        System.out.println("Итого подписчиков: " + system.getSubscriberCount());
    }
}

