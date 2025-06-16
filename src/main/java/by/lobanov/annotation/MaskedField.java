package by.lobanov.annotation;

import by.lobanov.annotation.constans.*;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MaskedField {

    MaskingStrategy strategy();
    char maskCharacter() default '*';
    int visibleChars() default 4;
}