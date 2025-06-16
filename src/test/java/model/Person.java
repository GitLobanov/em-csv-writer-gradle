package model;

import by.lobanov.annotation.constans.*;
import by.lobanov.annotation.csv.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@CsvRecord(defaultNamingStrategy = NamingStrategy.AS_IS_TO_SPACE_SEPARATED_CAPITALIZED)
public class Person {

    private String firstName;
    private String lastName;
    private int dayOfBirth;
    private Months monthOfBirth;
    private int yearOfBirth;
}
