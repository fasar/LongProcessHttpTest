package fr.fasar.LongProcessHttpTest.service;

public class TaskInterruptedException extends RuntimeException {
    public TaskInterruptedException() {
        super();
    }

    public TaskInterruptedException(String message) {
        super(message);
    }

    public TaskInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskInterruptedException(Throwable cause) {
        super(cause);
    }

    protected TaskInterruptedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
