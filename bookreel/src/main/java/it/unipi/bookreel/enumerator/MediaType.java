package it.unipi.bookreel.enumerator;

public enum MediaType {
    FILMS,
    BOOKS;

    public static MediaType fromString(String type) {
        try {
            return MediaType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo di media non valido: " + type);
        }
    }
}