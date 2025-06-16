package by.lobanov;

import by.lobanov.annotation.*;
import by.lobanov.annotation.csv.*;
import by.lobanov.exception.*;

import java.io.*;
import java.util.*;

/**
 * Общий интерфейс для записи списка объектов в некоторый формат.
 * Реализации определяют конкретный формат и способ обработки данных.
 *
 * @author Астонский Шпион
 */
public interface Writable extends Closeable {

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
     * @throws FormatRecordAnnotationMissingException Если класс объектов в списке не аннотирован Формат Рекорд (прим. {@link CsvRecord})
     * @throws IllegalArgumentException            Если объекты в списке разных типов.
     */
    void write(List<?> data) throws IOException;
}
