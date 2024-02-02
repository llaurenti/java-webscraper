import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webscraper.CrawlPostResponse;
import com.webscraper.InvalidKeywordLengthException;
import com.webscraper.ScraperService;

public class ScraperServiceTest {
    ScraperService service;

    @BeforeEach
    void setUp() {
        service = new ScraperService();
    }

    @Test
    public void throwExceptionWhenKeywordLessThan4Chars() throws InvalidKeywordLengthException {
        String keyword = "123";
        assertTrue(keyword.length() < 4);
        Assertions.assertThrows(InvalidKeywordLengthException.class,
                () -> service.crawl("{\"keyword\": \"" + keyword + "\"}"));
    }

    @Test
    public void throwExceptionWhenKeywordMoreThan32Chars() throws InvalidKeywordLengthException {
        String keyword = "afdasjklflk;adsjldf;;lksadjfas;dd";
        assertTrue(keyword.length() > 32);
        Throwable exception = Assertions.assertThrows(InvalidKeywordLengthException.class,
                () -> service.crawl("{\"keyword\": \"" + keyword + "\"}"));
        assertEquals("The keyword must be between 4 and 32 characters long", exception.getMessage());
    }

    @Test
    public void generateValidId() throws InvalidKeywordLengthException {
        CrawlPostResponse response = service.crawl("{\"keyword\": \"gugu\"}");
        final int expectedIdLength = 8;
        assertEquals("Z3VndQ00", response.getId());
        assertEquals(expectedIdLength, response.getId().length(), "Id should have length of 8");
    }

    @Test
    public void guaranteeThatIdGenerationIsIdempotent() throws InvalidKeywordLengthException {
        final String expectedResult = "dGV0aW5o";
        CrawlPostResponse response1 = service.crawl("{\"keyword\": \"tetinha\"}");
        assertEquals(expectedResult, response1.getId());
        CrawlPostResponse response2 = service.crawl("{\"keyword\": \"tetinha\"}");
        assertEquals(expectedResult, response2.getId());
    }

    @Test
    public void differentKeywordsShouldGenerateDifferentIds() throws InvalidKeywordLengthException {
        final String expectedResultForFirstKeyword = "Zmlyc3Rr";
        CrawlPostResponse firstKeywordResponse = service.crawl("{\"keyword\": \"firstkeyword\"}");
        assertEquals(expectedResultForFirstKeyword, firstKeywordResponse.getId());
        CrawlPostResponse response3DifferentKeyword = service.crawl("{\"keyword\": \"anotherkeyword\"}");
        assertNotEquals(expectedResultForFirstKeyword, response3DifferentKeyword.getId(),
                "Should be different than the first result");
    }
}
