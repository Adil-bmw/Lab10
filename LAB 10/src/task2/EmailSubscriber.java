package task2;

// подписчик, получающий уведомления по "email"
public class EmailSubscriber implements Subscriber {

    private final String email;

    public EmailSubscriber(String email) {
        this.email = email;
    }

    @Override
    public void onNotify(String message) {
        // симулируем отправку письма
        try {
            Thread.sleep(10);
            System.out.println("Email sent to " + email + ": " + message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
