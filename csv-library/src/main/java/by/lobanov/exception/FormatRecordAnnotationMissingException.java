package by.lobanov.exception;

public class FormatRecordAnnotationMissingException extends IllegalArgumentException {

    public FormatRecordAnnotationMissingException(String message) {
        super(message);
    }

    public FormatRecordAnnotationMissingException(Class<?> targetClass) {
        super(String.format("Класс '%s' не аннотирован @CsvRecord и не может быть обработан как CSV запись.",
                targetClass.getName()));
    }
}
