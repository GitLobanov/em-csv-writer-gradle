import by.lobanov.*;
import by.lobanov.annotation.*;
import by.lobanov.annotation.constans.*;
import by.lobanov.annotation.csv.*;
import lombok.*;
import model.*;
import util.*;

import java.io.*;
import java.util.*;

class MainTest {

    public static void main(String[] args) throws IOException {
        try (Writable writer = CsvWriterFactory.create(new FileWriter("user_list.csv"))) {
            List<User> users = new ArrayList<>();
            users.add(new User("Bob", "Ivanovitch", "1123-1313-1231-1414"));
            users.add(new User("Nicol", "Ivanovitch", "1841-1313-2355-1414"));
            users.add(new User("Vladimir", "Ivanovitch", "2809-9253-1231-1414"));

            writer.write(users);
        }

        try (Writable writer = CsvWriterFactory.create(new FileWriter("person_list.csv"))) {
            List<Person> persons = TestDataGenerator.generatePersons(3);
            writer.write(persons);
        }

        try (Writable writer = CsvWriterFactory.create(new FileWriter("clients_list.csv"))) {
            List<Client> clients = TestDataGenerator.generateClients(3);
            writer.write(clients);
        }

        try (Writable writer = CsvWriterFactory.create(new FileWriter("students_list.csv"))) {
            List<Student> students = TestDataGenerator.generateStudents(3);
            writer.write(students);
        }
    }

    @CsvRecord
    @AllArgsConstructor
    private static class User {

        private String firstName;
        private String lastName;
        @MaskedField(strategy = MaskingStrategy.ASTERISKS_PARTIAL_SUFFIX)
        private String accountNumber;
    }
}
