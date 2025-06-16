package util;

import lombok.experimental.*;
import model.*;
import net.datafaker.*;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

@UtilityClass
public class TestDataGenerator {

    private static final Faker faker = new Faker();
    private static final Random random = new Random();

    private static Months getRandomMonth() {
        Months[] months = Months.values();
        return months[random.nextInt(months.length)];
    }

    private static Months getMonthFromInt(int monthValue) {
        if (monthValue < 1 || monthValue > 12) {
            throw new IllegalArgumentException("Month value must be between 1 and 12");
        }
        return Months.values()[monthValue - 1];
    }

    public static List<Person> generatePersons(int count) {
        List<Person> persons = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            LocalDate birthDate = faker.timeAndDate().birthday(18, 65);
            Person person = Person.builder()
                    .firstName(faker.name().firstName())
                    .lastName(faker.name().lastName())
                    .dayOfBirth(birthDate.getDayOfMonth())
                    .monthOfBirth(getMonthFromInt(birthDate.getMonthValue()))
                    .yearOfBirth(birthDate.getYear())
                    .build();
            persons.add(person);
        }
        return persons;
    }

    public static List<Student> generateStudents(int count) {
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int numberOfScores = random.nextInt(2, 5);
            List<String> scores = IntStream.range(0, numberOfScores)
                    .mapToObj(j -> String.valueOf(faker.number().numberBetween(60, 100)))
                    .toList();

            Student student = Student.builder()
                    .name(faker.name().fullName())
                    .score(scores)
                    .build();
            students.add(student);
        }
        return students;
    }

    public static List<Client> generateClients(int count) {
        List<Client> clients = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double amount = faker.number().randomDouble(2, 100, 5000);

            Client client = Client.builder()
                    .firstName(faker.name().firstName())
                    .lastName(faker.name().lastName())
                    .accountNumber(faker.finance().iban("DE").replaceAll("\\s",""))
                    .amountToPay(BigDecimal.valueOf(amount).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .build();
            clients.add(client);
        }
        return clients;
    }

    public static void main(String[] args) {
        System.out.println("--- Persons ---");
        List<Person> persons = generatePersons(5);
        persons.forEach(p -> System.out.println(
                p.getFirstName() + " " + p.getLastName() + ", born " +
                        p.getDayOfBirth() + " " + p.getMonthOfBirth() + " " + p.getYearOfBirth()
        ));

        System.out.println("\n--- Students ---");
        List<Student> students = generateStudents(3);
        students.forEach(s -> System.out.println(
                s.getName() + ", Scores: " + String.join(", ", s.getScore())
        ));

        System.out.println("\n--- Clients ---");
        List<Client> clients = generateClients(3);
        clients.forEach(c -> System.out.println(
                c.getFirstName() + " " + c.getLastName() +
                        ", Acc: " + c.getAccountNumber() +
                        ", Pays: " + c.getAmountToPay()
        ));
    }
}