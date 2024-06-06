package app.exceptions;

public class DatabaseException extends Exception {
    public DatabaseException(String userMessage) {
        super(userMessage);
        System.out.println("userMessage: " + userMessage);
    }

    public DatabaseException(String userMessage, String systemMessage) {
        super(userMessage);
        System.out.println("userMessage: " + userMessage);
        System.out.println("errorMessage: " + systemMessage);
    }

    public DatabaseException(String userMessage, Exception e) {
        super(userMessage, e);
        System.out.println("userMessage: " + userMessage);
        System.out.println("Exception message: " + e.getMessage());
        System.out.println("Stack trace:");
        e.printStackTrace(System.out); // Udskriver staksporet til konsollen
    }
}
