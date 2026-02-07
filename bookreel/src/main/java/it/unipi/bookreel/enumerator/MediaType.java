package it.unipi.bookreel.enumerator;

public enum MediaType {
    FILM, //vedi se è meglio mettere MOVIES and BOOKS
    LIBRI;

    public static MediaType fromString(String type) {
        try {
            return MediaType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo di media non valido: " + type);
        }
    }
}