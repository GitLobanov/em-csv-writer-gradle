package by.lobanov;

import by.lobanov.impl.*;
import lombok.experimental.*;

import java.io.*;

/**
 * Фабрика по созданию CsvWriter
 *
 * @author Астонский Шпион
 */
@UtilityClass
public class CsvWriterFactory {

    private static final char DEFAULT_DELIMITER = ',';
    private static final String DEFAULT_LINE_SEPARATOR = "\n";
    private static final char TSV_DELIMITER = '\t';

    /**
     * Создает CsvWriter со стандартными настройками (разделитель ',', перенос строки '\n').
     * @param writer Writer для вывода данных.
     * @return Экземпляр Writable.
     */
    public static Writable create(Writer writer) {
        return new DefaultCsvWriter(writer, DEFAULT_DELIMITER, DEFAULT_LINE_SEPARATOR);
    }

    /**
     * Создает CsvWriter с указанным разделителем и стандартным переносом строки ('\n').
     * @param writer Writer для вывода данных.
     * @param delimiter Символ-разделитель.
     * @return Экземпляр Writable.
     */
    public static Writable create(Writer writer, char delimiter) {
        return new DefaultCsvWriter(writer, delimiter, DEFAULT_LINE_SEPARATOR);
    }

    /**
     * Создает CsvWriter с указанными разделителем и переносом строки.
     * @param writer Writer для вывода данных.
     * @param delimiter Символ-разделитель.
     * @param lineSeparator Строка для переноса строки.
     * @return Экземпляр Writable.
     */
    public static Writable create(Writer writer, char delimiter, String lineSeparator) {
        return new DefaultCsvWriter(writer, delimiter, lineSeparator);
    }

    /**
     * Создает CsvWriter для формата TSV (Tab-Separated Values).
     * @param writer Writer для вывода данных.
     * @return Экземпляр Writable, настроенный для TSV.
     */
    public static Writable createTsvWriter(Writer writer) {
        return new DefaultCsvWriter(writer, TSV_DELIMITER, DEFAULT_LINE_SEPARATOR);
    }
}
