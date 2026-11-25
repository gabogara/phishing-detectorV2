package com.gabriel.phishingagent.exception;

public final class ErrorMessages {

    public static final int MAX_LENGTH = 500;

    private ErrorMessages() {
    }

    public static String describe(Throwable error) {
        if (error == null) {
            return "Unknown error";
        }

        String type = error.getClass().getSimpleName();
        if (type.isBlank()) {
            type = "Error"; // anonymous classes have no simple name
        }

        String message = error.getMessage();
        String text = (message == null || message.isBlank())
                ? type
                : type + ": " + message.replaceAll("\\s+", " ").strip();

        return text.length() <= MAX_LENGTH ? text : text.substring(0, MAX_LENGTH - 3) + "...";
    }
}