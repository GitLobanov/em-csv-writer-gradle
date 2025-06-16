package model;

import by.lobanov.annotation.csv.*;
import lombok.*;
import java.util.*;

@Data
@Builder
@AllArgsConstructor
@CsvRecord
public class Student {

    private String name;
    private List<String> score;
}