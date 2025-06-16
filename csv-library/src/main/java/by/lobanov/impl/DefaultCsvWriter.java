package by.lobanov.impl;

import by.lobanov.*;
import by.lobanov.annotation.*;
import by.lobanov.annotation.constans.*;
import by.lobanov.annotation.csv.*;
import by.lobanov.exception.*;
import lombok.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

/**
 * Реализация записи в формате CSV
 *
 * @author Астонский Шпион
 */
public class DefaultCsvWriter implements Writable {

    private final Writer writer;
    private final char delimiter;
    private final String lineSeparator;

    /**
     * Конструктор.
     *
     * @param writer        Куда будут записываться данные.
     * @param delimiter     Символ-разделитель полей.
     * @param lineSeparator Символ(ы) для разделения строк.
     */
    public DefaultCsvWriter(Writer writer, char delimiter, String lineSeparator) {
        Objects.requireNonNull(writer, "Writer не может быть null");
        Objects.requireNonNull(lineSeparator, "Line separator не может быть null");
        if (lineSeparator.isEmpty()) {
            throw new IllegalArgumentException("Line separator не может быть пустым");
        }
        this.writer = writer;
        this.delimiter = delimiter;
        this.lineSeparator = lineSeparator;
    }

    /**
     * Записывает список объектов (POJO) в CSV формат, используя предоставленный {@link Writer}.
     * <p>
     * Метод определяет структуру CSV на основе первого не {@code null} объекта в списке.
     * Все объекты в списке должны быть одного типа и аннотированы {@link CsvRecord}.
     * Поля для записи определяются на основе рефлексии и аннотаций {@link DataField},
     * {@link TransientField} и {@link MaskedField}.
     * </p>
     *
     * @param data Список объектов для записи. Если список {@code null}, пуст, или первый объект {@code null},
     *             метод не выполняет никаких действий (кроме выброса исключения, если аннотация отсутствует).
     * @throws IOException                         Если возникает ошибка ввода-вывода во время записи.
     * @throws FormatRecordAnnotationMissingException Если класс объектов в списке не аннотирован {@link CsvRecord}.
     * @throws IllegalArgumentException            Если объекты в списке разных типов.
     */
    @Override
    public void write(List<?> data) throws IOException {
        if (isInvalidData(data)) return;

        Object firstObject = data.get(0);
        Class<?> clazz = firstObject.getClass();
        CsvRecord csvRecordAnnotation = clazz.getAnnotation(CsvRecord.class);
        boolean includeHeader = (csvRecordAnnotation == null) || csvRecordAnnotation.includeHeader();
        NamingStrategy classNamingStrategy = (csvRecordAnnotation != null) ?
                csvRecordAnnotation.defaultNamingStrategy() : NamingStrategy.AS_IS_TO_SPACE_SEPARATED_CAPITALIZED;

        List<ProcessedField> processedFields = processFields(clazz, classNamingStrategy);
        if (processedFields.isEmpty()) return;

        processHeaders(includeHeader, processedFields);
        processDataFields(data, processedFields, clazz);
    }

    /**
     * Проверяет валидность входного списка данных.
     *
     * @param data Список данных для проверки.
     * @return {@code true}, если данные невалидны ({@code null}, пустые, первый элемент {@code null}),
     *         {@code false} в противном случае.
     * @throws FormatRecordAnnotationMissingException если класс первого объекта не аннотирован {@link CsvRecord}.
     */
    private static boolean isInvalidData(List<?> data) {
        boolean isInvalid = data == null || data.isEmpty() || data.get(0) == null;
        if (isInvalid) return true;
        Class<?> clazz = data.get(0).getClass();
        if (clazz.getAnnotation(CsvRecord.class) == null) {
            throw new FormatRecordAnnotationMissingException(clazz);
        }
        return false;
    }

    /**
     * Обрабатывает и записывает данные из списка объектов.
     *
     * @param data            Список объектов для записи.
     * @param processedFields Список обработанных полей, определяющих структуру CSV.
     * @param clazz           Класс объектов в списке.
     * @throws IOException              Если возникает ошибка ввода-вывода.
     * @throws IllegalArgumentException Если объект в списке не является экземпляром {@code clazz}.
     */
    private void processDataFields(List<?> data, List<ProcessedField> processedFields, Class<?> clazz) throws IOException {
        for (Object obj : data) {
            if (obj == null) {
                writeRowInternal(Collections.nCopies(processedFields.size(), null));
                continue;
            }
            if (!clazz.isInstance(obj)) {
                throw new IllegalArgumentException("Все объекты в списке должны быть одного типа: " + clazz.getName() +
                        ", встречен: " + obj.getClass().getName());
            }
            List<String> rowValues = new ArrayList<>();
            for (ProcessedField pf : processedFields) {
                try {
                    Object value = pf.getField().get(obj);
                    String stringValue = convertFieldValueToString(value, pf);
                    rowValues.add(stringValue);
                } catch (IllegalAccessException e) {
                    throw new IOException("Ошибка доступа к полю: " + pf.getField().getName(), e);
                }
            }
            writeRowInternal(rowValues);
        }
    }

    /**
     * Записывает строку заголовков, если это необходимо.
     *
     * @param includeHeader   {@code true}, если заголовок должен быть записан.
     * @param processedFields Список обработанных полей, из которых формируются заголовки.
     * @throws IOException Если возникает ошибка ввода-вывода.
     */
    private void processHeaders(boolean includeHeader, List<ProcessedField> processedFields) throws IOException {
        if (includeHeader) {
            List<String> headers = processedFields.stream()
                    .map(ProcessedField::getHeaderName)
                    .collect(Collectors.toList());
            writeRowInternal(headers);
        }
    }

    /**
     * Записывает одну строку данных (список строковых значений) в CSV.
     * Экранирует и заключает в кавычки значения при необходимости.
     *
     * @param row Список строковых значений для записи в качестве одной строки CSV.
     *            Если {@code row} равен {@code null}, записывается только разделитель строк.
     * @throws IOException Если возникает ошибка ввода-вывода.
     */
    private void writeRowInternal(List<String> row) throws IOException {
        if (row == null) {
            writer.append(lineSeparator);
            return;
        }
        for (int i = 0; i < row.size(); i++) {
            writer.append(escapeAndQuote(row.get(i)));
            if (i < row.size() - 1) {
                writer.append(delimiter);
            }
        }
        writer.append(lineSeparator);
    }

    /**
     * Анализирует поля класса для определения, какие из них должны быть включены в CSV,
     * и какие имена заголовков им соответствуют.
     *
     * @param clazz               Класс для анализа.
     * @param classNamingStrategy Стратегия именования, применяемая на уровне класса (по умолчанию).
     * @return Список объектов {@link ProcessedField}, представляющих поля для CSV.
     */
    private List<ProcessedField> processFields(Class<?> clazz, NamingStrategy classNamingStrategy) {
        List<ProcessedField> tempFields = new ArrayList<>();
        Field[] declaredFields = clazz.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(TransientField.class) ||
                    java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);

            String headerName = getHeader(field, classNamingStrategy);

            tempFields.add(new ProcessedField(field, headerName,
                    field.getAnnotation(CsvRecord.class),
                    field.getAnnotation(MaskedField.class)));
        }

        return tempFields;
    }

    private String getHeader(Field field, NamingStrategy fieldNamingStrategy) {
        String headerName;
        DataField dataField = field.getAnnotation(DataField.class);
        if (dataField != null) {
            if (!dataField.name().isEmpty()) {
                headerName = dataField.name();
            } else {
                if (dataField.strategy() != NamingStrategy.DEFAULT) {
                    fieldNamingStrategy = dataField.strategy();
                }
                headerName = applyNamingStrategy(field.getName(), fieldNamingStrategy);
            }
        } else {
            headerName = applyNamingStrategy(field.getName(), fieldNamingStrategy);
        }
        return headerName;
    }

    private String applyNamingStrategy(String fieldName, NamingStrategy strategy) {
        if (fieldName == null || fieldName.isEmpty()) {
            return "";
        }

        if (strategy == null) {
            strategy = NamingStrategy.AS_IS;
        }

        return switch (strategy) {
            case AS_IS_TO_SPACE_SEPARATED_CAPITALIZED -> {
                StringBuilder result = new StringBuilder();
                result.append(Character.toUpperCase(fieldName.charAt(0)));
                for (int i = 1; i < fieldName.length(); i++) {
                    char currentChar = fieldName.charAt(i);
                    if (Character.isUpperCase(currentChar)) {
                        result.append(' ');
                    }
                    result.append(currentChar);
                }
                yield result.toString();
            }
            case CAMEL_TO_SNAKE_CASE -> fieldName.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
            case CAMEL_TO_SCREAMING_SNAKE_CASE -> fieldName.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();
            case DEFAULT, AS_IS -> fieldName;
        };
    }

    private String convertFieldValueToString(Object value, ProcessedField processedField) {
        if (value == null) {
            return "";
        }

        MaskedField maskedField = processedField.getMaskedField();
        String stringValue = value.toString();

        if (maskedField != null) {
            stringValue = applyMasking(stringValue, maskedField);
        }
        return stringValue;
    }

    private String applyMasking(String originalValue, MaskedField maskedField) {
        if (originalValue == null || originalValue.isEmpty()) return "";

        MaskingStrategy strategy = maskedField.strategy();
        char maskChar = maskedField.maskCharacter();
        int visibleChars = maskedField.visibleChars();

        return switch (strategy) {
            case ASTERISKS_FULL -> repeatChar(maskChar, originalValue.length());
            case ASTERISKS_PARTIAL_PREFIX -> {
                if (originalValue.length() <= visibleChars) yield originalValue;
                yield originalValue.substring(0, visibleChars) + repeatChar(maskChar, originalValue.length() - visibleChars);
            }
            case ASTERISKS_PARTIAL_SUFFIX -> {
                if (originalValue.length() <= visibleChars) yield originalValue;
                yield repeatChar(maskChar, originalValue.length() - visibleChars) + originalValue.substring(originalValue.length() - visibleChars);
            }
        };
    }

    private String repeatChar(char c, int times) {
        if (times <= 0) return "";
        char[] chars = new char[times];
        Arrays.fill(chars, c);
        return new String(chars);
    }

    private String escapeAndQuote(String value) {
        if (value == null) {
            return "";
        }

        boolean needsQuoting = false;
        if (value.indexOf(delimiter) != -1 ||
                value.indexOf('\n') != -1 ||
                value.indexOf('\r') != -1 ||
                value.indexOf('"') != -1) {
            needsQuoting = true;
        }
        if (value.contains(this.lineSeparator)) {
            needsQuoting = true;
        }


        String result = value.replace("\"", "\"\"");

        if (needsQuoting) {
            return "\"" + result + "\"";
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    @Getter
    @AllArgsConstructor
    private static class ProcessedField {
        private final Field field;
        private final String headerName;
        private final CsvRecord csvRecord;
        private final MaskedField maskedField;
    }
}
