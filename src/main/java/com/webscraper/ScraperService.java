package com.webscraper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;

public class ScraperService {
    private static String BASE_URL;
    private static ScrapedDataPool pool;

    public ScraperService() {
        BASE_URL = System.getenv("BASE_URL");
        pool = ScrapedDataPool.getInstance();
    }

    public ScrapedData getScrapedData(String id) {
        return pool.get(id);
    }

    public CrawlPostResponse crawl(String body) throws InvalidKeywordLengthException {
        String keyword = parseKeyword(body);
        validate(keyword);
        String id = generateId(keyword);
        pool.initializeNewScrapeData(id);
        scrape(id, keyword, BASE_URL);
        return new CrawlPostResponse(id);
    }

    public void scrape(String crawlId, String keyword, String url) {
        if (keyword == null)
            return;
        Set<String> scrapedUrls = new HashSet<>();
        String htmlContent = getHtmlContentFrom(url);
        if (htmlContent == null) {
            return;
        }
        if (hasKeyword(htmlContent, keyword))
            scrapedUrls.add(url);
        pool.add(crawlId, scrapedUrls);
    }
    public String getHtmlContentFrom(String url) {
        String htmlContent = null;
        try {
            final HttpClient client = new HttpClient();
            client.start();
            final ContentResponse res = client.GET(url);
            htmlContent = res.getContentAsString();
            client.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return htmlContent;
    }
    private boolean hasKeyword(String htmlAsString, String keyword) {
        if (htmlAsString == null) {
            return false;
        }
        final Pattern p = Pattern.compile("\\s" + keyword + "\\s", Pattern.CASE_INSENSITIVE);
        final Matcher matcher = p.matcher(htmlAsString);
        return matcher.find();
    }

    private void validate(String body) throws InvalidKeywordLengthException {
        if (body.length() < 4 || body.length() > 32) {
            throw new InvalidKeywordLengthException();
        }
    }

    private String parseKeyword(String body) {
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
}
