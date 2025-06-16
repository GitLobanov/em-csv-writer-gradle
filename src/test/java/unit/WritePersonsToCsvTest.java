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

class WritePersonsToCsvTest {

    private Path testFilePath;

    private static final String TEST_FILE_BASENAME = "person_list.csv";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp () {
        testFilePath = tempDir.resolve(TEST_FILE_BASENAME);
    }

    private void prepareAndWriteToFile(List<?> data) {
        try (Writer fileWriter = new FileWriter(testFilePath.toString());
             Writable writerInstance = CsvWriterFactory.create(fileWriter)) {
            writerInstance.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void givenListOfPersons_whenWriteToCsvFile_thenFileShouldBeExist() {
        // given
        List<Person> persons = TestDataGenerator.generatePersons(3);

        // when
        prepareAndWriteToFile(persons);

        // then
        assertTrue(Files.exists(testFilePath));
    }

    @Test
    void givenListOfPersons_whenWriteToCsvFile_thenFileShouldBeNotEmpty() throws IOException {
        // given
        List<Person> persons = TestDataGenerator.generatePersons(3);

        // when
        prepareAndWriteToFile(persons);

        // then
        assertTrue(Files.size(testFilePath) > 0);
    }

    @Test
    void givenListOfPersons_whenWriteToCsvFile_thenFileShouldBeSizeOfListPersons () {
        // given
        List<Person> persons = TestDataGenerator.generatePersons(3);

        // when
        prepareAndWriteToFile(persons);

        // then
        List<String> actualLines = getDataLines();

        assertEquals(actualLines.size(), persons.size());
    }

    @Test
    void givenListOfPersons_whenHaveNamingStrategyCapitalized_thenFileShouldBeContainsCapitalizedHeaders () {
        // given
        List<Person> persons = TestDataGenerator.generatePersons(3);

        // when
        prepareAndWriteToFile(persons);

        // then
        String headers = getHeaders();

        assertEquals("First Name,Last Name,Day Of Birth,Month Of Birth,Year Of Birth", headers);
    }

    private String getHeaders () {
        try {
            return Files.readAllLines(testFilePath).get(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getDataLines () {
        List<String> copy = null;
        try {
            copy = Files.readAllLines(testFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        copy.remove(0);
        return copy;
    }
}
