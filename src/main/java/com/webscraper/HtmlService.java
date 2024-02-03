package com.webscraper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;

public class HtmlService {
    private static ScrapedDataPool pool;

    public HtmlService() {
        pool = ScrapedDataPool.getInstance();
    }

    public Set<String> parseUrlsContainedIn(String url) {
        Set<String> response = new HashSet<>();
        if ((response = pool.getCachedContainedUrls(url)) != null) {
            return response;
        }
        response = new HashSet<>();
        String htmlContent = getHtmlContentFrom(url);
        if (htmlContent == null)
            return response;
        final Pattern p = Pattern.compile("(?<=href=\").+?(?=\")", Pattern.CASE_INSENSITIVE);
        final Matcher matcher = p.matcher(htmlContent);
        while (matcher.find()) {
            response.add(matcher.group());
        }
        String baseUrl = System.getenv("BASE_URL");
        return new HashSet<>(response.stream()
                .filter((u) -> !u.matches("^[a-zA-Z]"))
                .map((u) -> {
                    if (u.contains("http") || u.contains("https")) {
                        return u;
                    }
                    return baseUrl + u;
                })
                .filter((u) -> u.startsWith(baseUrl))
                .filter((u) -> u.endsWith("html") || u.endsWith(("htm"))) // temporary?
                .toList());
    }

    private String getHtmlContentFrom(String url) {
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

    public Set<String> parseUniqueKeywordsFromUrl(String url) {
        String content = getHtmlContentFrom(url);
        return new HashSet<>(Arrays.asList(content.split("\\s")).stream()
                .filter((s) -> s.length() >= 4 && s.length() <= 32)
                .toList());
    }
        }
