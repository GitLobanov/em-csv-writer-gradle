package unit;

import by.lobanov.*;
import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.*;
import util.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WriteStudentsToCsvTest {

    private Path testFilePath;
    private static final String TEST_FILE_BASENAME = "students_output.csv";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        testFilePath = tempDir.resolve(TEST_FILE_BASENAME);
    }

    private void prepareAndWriteToFile(List<?> data) {
        try (Writer fileWriter = new FileWriter(testFilePath.toString());
             Writable writerInstance = CsvWriterFactory.create(fileWriter)) {
            writerInstance.write(data);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при записи в файл: " + e.getMessage(), e);
        }
    }

    private List<String> readAllLinesFromFile() {
        try {
            return Files.readAllLines(testFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла: " + e.getMessage(), e);
        }
    }

    @Test
    void givenListOfStudents_whenWriteToCsvFile_thenFileShouldExistAndNotEmpty() throws IOException {
        // given
        List<Student> students = TestDataGenerator.generateStudents(2);

        // when
        prepareAndWriteToFile(students);

        // then
        assertTrue(Files.exists(testFilePath), "Файл должен существовать");
        assertTrue(Files.size(testFilePath) > 0, "Файл не должен быть пустым");
    }

    @Test
    void givenListOfStudents_whenWriteToCsvFile_thenCorrectNumberOfLinesShouldBeWritten() {
        // given
        List<Student> students = TestDataGenerator.generateStudents(3);
        int expectedLineCount = students.size() + 1; // +1 для заголовка

        // when
        prepareAndWriteToFile(students);

        // then
        List<String> actualLines = readAllLinesFromFile();
        assertEquals(expectedLineCount, actualLines.size(), "Неверное количество строк в файле");
    }

    @Test
    void givenListOfStudents_whenWriteToCsvFile_thenHeadersShouldMatchNamingStrategy() {
        // given
        List<Student> students = TestDataGenerator.generateStudents(1); // Достаточно одного для проверки заголовка

        // when
        prepareAndWriteToFile(students);

        // then
        List<String> actualLines = readAllLinesFromFile();
        assertFalse(actualLines.isEmpty(), "Файл не должен быть пустым, чтобы проверить заголовок");
        assertEquals("name,score", actualLines.get(0), "Заголовок не соответствует ожидаемому");
    }

    @Test
    void givenListOfStudents_whenWriteToCsvFile_thenDataShouldBeWrittenCorrectly() {
        // given
        List<Student> students = TestDataGenerator.generateStudents(2);
        // Сгенерируем студентов с предсказуемыми данными для этого теста
        students.clear();
        students.add(Student.builder().name("Alice Wonderland").score(List.of("90", "85")).build());
        students.add(Student.builder().name("Bob The Builder").score(List.of("70", "75", "80")).build());


        // when
        prepareAndWriteToFile(students);

        // then
        List<String> actualLines = readAllLinesFromFile();
        assertEquals(3, actualLines.size()); // 1 заголовок + 2 студента

        // Проверяем данные Alice
        // List<String> score будет как "[90, 85]" из-за value.toString()
        // Если строка содержит запятую, она будет экранирована кавычками.
        String expectedAliceLine = String.format("%s,\"%s\"",
                students.get(0).getName(),
                students.get(0).getScore().toString() // toString() для List<String>
        );
        assertEquals(expectedAliceLine, actualLines.get(1));

        // Проверяем данные Bob
        String expectedBobLine = String.format("%s,\"%s\"",
                students.get(1).getName(),
                students.get(1).getScore().toString()
        );
        assertEquals(expectedBobLine, actualLines.get(2));
    }

    @Test
    void givenStudentWithCommaInName_whenWriteToCsvFile_thenNameShouldBeQuoted() {
        // given
        List<Student> students = List.of(
                Student.builder().name("Smith, John").score(List.of("100")).build()
        );

        // when
        prepareAndWriteToFile(students);

        // then
        List<String> actualLines = readAllLinesFromFile();
        String expectedDataLine = String.format("\"%s\",%s",
                "Smith, John",
                students.get(0).getScore()
        );
        assertEquals(expectedDataLine, actualLines.get(1));
    }
}
