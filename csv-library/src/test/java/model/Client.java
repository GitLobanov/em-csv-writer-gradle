package model;


import by.lobanov.annotation.*;
import by.lobanov.annotation.constans.*;
import by.lobanov.annotation.csv.*;
import lombok.*;

import java.math.*;

@Data
@Builder
@AllArgsConstructor
@CsvRecord(defaultNamingStrategy = NamingStrategy.CAMEL_TO_SCREAMING_SNAKE_CASE)
public class Client {

    private String firstName;
    @TransientField
    private String lastName;
    @MaskedField(maskCharacter = 'X', strategy = MaskingStrategy.ASTERISKS_PARTIAL_PREFIX)
    private String accountNumber;
    private BigDecimal amountToPay;
}
