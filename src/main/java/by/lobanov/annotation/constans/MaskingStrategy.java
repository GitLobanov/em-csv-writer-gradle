package by.lobanov.annotation.constans;

public enum MaskingStrategy {

    /**
     * Полностью заменяет значение на символы маскирования (например, "password123" -> "***********").
     */
    ASTERISKS_FULL,
    /**
     * Оставляет видимыми несколько символов в конце, остальное маскирует (например, "1234567890" -> "*******890" с visibleChars=3).
     */
    ASTERISKS_PARTIAL_SUFFIX,
    /**
     * Оставляет видимыми несколько символов в начале, остальное маскирует (например, "1234567890" -> "123*******" с visibleChars=3).
     */
    ASTERISKS_PARTIAL_PREFIX
}
