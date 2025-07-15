package by.lobanov;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        try (Writable writer = CsvWriterFactory.create(new FileWriter("user_list.csv"))) {
            List<User> users = new ArrayList<>();
            users.add(new User("Bob", "Ivanovitch", "1123-1313-1231-1414"));
            users.add(new User("Nicol", "Ivanovitch", "1841-1313-2355-1414"));
            users.add(new User("Vladimir", "Ivanovitch", "2809-9253-1231-1414"));

            writer.write(users);
        }
    }
}