package com.webscraper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.util.log.Slf4jLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScraperService {
    private static ScrapedDataPool pool;
    private HtmlService htmlService;
    private static Logger logger = LoggerFactory.getLogger(Slf4jLog.class);
    private ExecutorService es = Executors.newCachedThreadPool();

    public ScraperService() {
        pool = ScrapedDataPool.getInstance();
        htmlService = new HtmlService();
    }

    public ScrapedData getScrapedData(String id) {
        return pool.getScrapedData(id);
    }

    public CrawlPostResponse crawl(String body) throws InvalidKeywordLengthException {
        String keyword = parseKeywordFromBody(body);
        validate(keyword);
        String id = generateId(keyword);
        if (pool.isKeywordCached(id)) {
            return new CrawlPostResponse(id);
        }
        pool.init(id);
        scrape(id, keyword);
        return new CrawlPostResponse(id);
    }

    private void validate(String body) throws InvalidKeywordLengthException {
        if (body == null || "".equals(body) || body.length() < 4 || body.length() > 32) {
            throw new InvalidKeywordLengthException();
        }
    }

    private String parseKeywordFromBody(String body) {
        Pattern pattern = Pattern.compile("\"keyword\":\\s*\"(.*)\"");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return body;
    }

    private String generateId(String key) {
        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
        String nonAlphanumericCharsRegex = "[^A-Za-z0-9]";
        return Base64.getEncoder().encodeToString(bytes)
                .replaceAll(nonAlphanumericCharsRegex, "0")
                .substring(0, 8);
    }

    public void scrape(String crawlId, String keyword) {
        boolean shouldEndRecursion = pool.isUrlsQueueEmpty();
        if (shouldEndRecursion) {
            try {
                es.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pool.finishScraping(crawlId);
            return;
        }
        String url = pool.pollUrl();
        logger.info("\nURL: " + url);
        Set<String> innerUrls = htmlService.parseUrlsContainedIn(url);
        pool.addUnscrapedUrlsToQueue(innerUrls);
        addScrapedDataToPoolIfContainsKeyword(crawlId, url, keyword, innerUrls);
        es.submit(new ScrapeRunnable(this, crawlId, keyword));
    }

    private void addScrapedDataToPoolIfContainsKeyword(String id, String url, String keyword, Set<String> innerUrls) {
        Set<String> uniqueKeywords = htmlService.parseUniqueKeywordsFromUrl(url);
        pool.addUrlToContentSet(url, uniqueKeywords, innerUrls);
        if (uniqueKeywords.contains(keyword)) {
            pool.addUrl(id, url);
        }
    }

}
