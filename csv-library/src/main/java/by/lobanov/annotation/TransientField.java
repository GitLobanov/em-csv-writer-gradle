package by.lobanov.annotation;

import java.lang.annotation.*;

/**
 * Указывает, что аннотированное поле должно быть исключено
 * из процесса сериализации/десериализации.
 *
 * @author Астонский Шпион
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TransientField {
}
