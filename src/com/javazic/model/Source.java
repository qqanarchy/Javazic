package com.javazic.model;

public enum Source {
    LOCAL,
    JAMENDO,
    APPLE_ITUNES;

    public boolean estDistant() {
        return this != LOCAL;
    }

    public String getTag() {
        return switch (this) {
            case LOCAL -> "   ";
            case JAMENDO -> "[J]";
            case APPLE_ITUNES -> "[A]";
        };
    }

    public String getLibelle() {
        return switch (this) {
            case LOCAL -> "Local";
            case JAMENDO -> "Jamendo";
            case APPLE_ITUNES -> "Apple";
        };
    }
}
