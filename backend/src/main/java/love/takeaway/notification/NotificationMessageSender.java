package love.takeaway.notification;

public interface NotificationMessageSender {

    SendResult send(NotificationOutbox message);

    record SendResult(boolean success, boolean retryable, boolean noPermission, String error) {
        public static SendResult delivered() {
            return new SendResult(true, false, false, null);
        }

        public static SendResult retry(String error) {
            return new SendResult(false, true, false, error);
        }

        public static SendResult fail(String error) {
            return new SendResult(false, false, false, error);
        }

        public static SendResult noPermission(String error) {
            return new SendResult(false, false, true, error);
        }
    }
}
