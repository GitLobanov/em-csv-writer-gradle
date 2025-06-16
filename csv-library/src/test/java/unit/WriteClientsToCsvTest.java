package unit;

import by.lobanov.*;
import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.*;
import util.*;

import java.io.*;
import java.math.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WriteClientsToCsvTest {

    private Path testFilePath;
    private static final String TEST_FILE_BASENAME = "clients_output.csv";

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
    void givenListOfClients_whenWriteToCsvFile_thenFileShouldExistAndNotEmpty() throws IOException {
        // given
        List<Client> clients = TestDataGenerator.generateClients(2);

        // when
        prepareAndWriteToFile(clients);

        // then
        assertTrue(Files.exists(testFilePath), "Файл должен существовать");
        assertTrue(Files.size(testFilePath) > 0, "Файл не должен быть пустым");
    }

    @Test
    void givenListOfClients_whenWriteToCsvFile_thenCorrectNumberOfLinesShouldBeWritten() {
        // given
        List<Client> clients = TestDataGenerator.generateClients(3);
        int expectedLineCount = clients.size() + 1; // +1 для заголовка

        // when
        prepareAndWriteToFile(clients);

        // then
        List<String> actualLines = readAllLinesFromFile();
        assertEquals(expectedLineCount, actualLines.size(), "Неверное количество строк в файле");
    }

    @Test
    void givenListOfClients_whenWriteToCsvFile_thenHeadersShouldMatchNamingStrategyAndExcludeTransient() {
        // given
        List<Client> clients = TestDataGenerator.generateClients(1);

        // when
        prepareAndWriteToFile(clients);

        // then
        List<String> actualLines = readAllLinesFromFile();
        assertFalse(actualLines.isEmpty(), "Файл не должен быть пустым, чтобы проверить заголовок");
        // @CsvRecord(defaultNamingStrategy = NamingStrategy.CAMEL_TO_SCREAMING_SNAKE_CASE)
        // Поля: firstName, accountNumber, amountToPay (lastName is @CsvTransient)
        assertEquals("FIRST_NAME,ACCOUNT_NUMBER,AMOUNT_TO_PAY", actualLines.get(0), "Заголовок не соответствует ожидаемому");
    }

    @Test
    void givenListOfClients_whenWriteToCsvFile_thenAccountNumberShouldBeMaskedAndDataCorrect() {
        // given
        // Используем предсказуемые данные для проверки маскирования
        List<Client> clients = List.of(
                Client.builder()
                        .firstName("TestFirstName")
                        .lastName("ShouldBeIgnored") // @CsvTransient
                        .accountNumber("1234567890123456")
                        .amountToPay(new BigDecimal("123.45"))
                        .build()
        );

        // when
        prepareAndWriteToFile(clients);

        // then
        List<String> actualLines = readAllLinesFromFile();
        assertEquals(2, actualLines.size());

        // "1234567890123456" -> "1234XXXXXXXXXXXX"
        String expectedAccountNumberMasked = "1234XXXXXXXXXXXX";
        String expectedDataLine = String.format("%s,%s,%s",
                clients.get(0).getFirstName(),
                expectedAccountNumberMasked,
                clients.get(0).getAmountToPay().toString() // toString() для BigDecimal
        );
        assertEquals(expectedDataLine, actualLines.get(1), "Строка данных клиента не соответствует ожидаемой (с учетом маскирования)");
    }

    @Test
    void givenClientWithShortAccountNumber_whenMasked_thenOriginalValueShouldBeReturned() {
        // given
        // Аккаунт короче, чем visibleChars (по умолчанию 4)
        List<Client> clients = List.of(
                Client.builder()
                        .firstName("ShortAcc")
                        .lastName("NoMatter")
                        .accountNumber("123") // < 4 символов
                        .amountToPay(new BigDecimal("10.00"))
                        .build()
        );

        // when
        prepareAndWriteToFile(clients);

        // then
        List<String> actualLines = readAllLinesFromFile();
        String expectedDataLine = String.format("%s,%s,%s",
                clients.get(0).getFirstName(),
                "123", // Оригинальное значение, так как originalValue.length() <= visibleChars
                clients.get(0).getAmountToPay().toString()
        );
        assertEquals(expectedDataLine, actualLines.get(1));
    }
}
