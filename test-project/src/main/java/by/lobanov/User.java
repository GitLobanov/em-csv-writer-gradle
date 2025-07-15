package by.lobanov;

import by.lobanov.annotation.*;
import by.lobanov.annotation.constans.*;
import by.lobanov.annotation.csv.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@CsvRecord(defaultNamingStrategy = NamingStrategy.AS_IS_TO_SPACE_SEPARATED_CAPITALIZED)
public class User {
    private String firstName;
    private String lastName;
    @MaskedField(strategy = MaskingStrategy.ASTERISKS_PARTIAL_SUFFIX)
    private String accountNumber;
}