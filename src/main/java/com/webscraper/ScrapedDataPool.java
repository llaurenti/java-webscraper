package com.webscraper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jetty.util.log.Slf4jLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ScrapedDataPool {
    private static Logger logger = LoggerFactory.getLogger(Slf4jLog.class);
    private static volatile ScrapedDataPool instance;
    private HashMap<String, ScrapedData> scrapedDataMap;
    private Set<UrlContent> urlContentSet;
    private ConcurrentLinkedQueue<String> queue;

    private ScrapedDataPool() {
        scrapedDataMap = new HashMap<>();
        urlContentSet = new HashSet<>();
        queue = new ConcurrentLinkedQueue<String>();
    }

    public static ScrapedDataPool getInstance() {
        /*
         * The approach taken here is called double-checked locking (DCL). It
         * exists to prevent race condition between multiple threads that may
         * attempt to get singleton instance at the same time, creating separate
         * instances as a result.
         * 
         * It may seem that having the `localRef` variable here is completely
         * pointless. There is, however, a very important caveat when
         * implementing double-checked locking in Java, which is solved by
         * introducing this local variable.
         * 
         * You can read more info DCL issues in Java here:
         * https://refactoring.guru/java-dcl-issue
         * - comment copied from the above website
         */
        ScrapedDataPool localRef = instance;
        if (localRef != null)
            return localRef;
        synchronized (ScrapedDataPool.class) {
            if (instance == null)
                instance = new ScrapedDataPool();
            return instance;
        }
    }

    public ScrapedData getScrapedData(String id) {
        return scrapedDataMap.get(id);
    }

    public void init(String id) {
        addStartingUrlToQueue();
        scrapedDataMap.putIfAbsent(id, new ScrapedData(id));
    }

    }
    private void addStartingUrlToQueue() {
        queue.add(System.getenv("BASE_URL"));
    }

    public void addUrlToContentSet(String url, Set<String> uniqueKeywords, Set<String> innerUrls) {
        urlContentSet.add(new UrlContent(url, uniqueKeywords, innerUrls));
    }

    public void addUrl(String id, String url) {
        if (!scrapedDataMap.containsKey(id)) {
            return;
        }
        ScrapedData aux = scrapedDataMap.get(id);
        aux.addUrl(url);
        scrapedDataMap.put(id, aux);
    }

    public Set<String> getKeywordsFrom(String url) {
        Optional<Set<String>> optionalContent = urlContentSet
                .stream()
                .filter((c) -> c.getUrl().equals(url))
                .map((c) -> c.getKeywords())
                .findFirst();
        return optionalContent.isPresent() ? optionalContent.get() : null;
    }

    public boolean isUrlsQueueEmpty() {
        logger.info("queue size: " + queue.size());
        return queue.isEmpty();
    }

    public String pollUrl() {
        return queue.poll();
    }

    public boolean isKeywordCached(String id) {
        return scrapedDataMap.get(id) != null;
    }

    public ScrapedData getCachedScrapedData(String id) {
        return scrapedDataMap.get(id);
    }

    public void finishScraping(String id) {
        ScrapedData aux = scrapedDataMap.get(id);
        aux.setStatus("done");
        scrapedDataMap.put(id, aux);
    }

    public void addUnscrapedUrlsToQueue(Set<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        queue.addAll(filterUrlsThatShouldBePutIntoQueue(urls));
    }

    private Set<String> filterUrlsThatShouldBePutIntoQueue(Set<String> urls) {
        Set<String> urlsToFilter = new HashSet<>(
                urlContentSet.stream().map((urlContent) -> urlContent.getUrl()).toList());
        scrapedDataMap.values().stream()
                .forEach((scrapedData) -> urlsToFilter.addAll(scrapedData.getUrls()));
        return new HashSet<>(urls.stream()
                .filter((url) -> !queue.contains(url) && !urlsToFilter.contains(url)).toList());
    }

    public Set<String> getCachedContainedUrls(String url) {
        Optional<UrlContent> optional = urlContentSet.stream().filter((uc) -> uc.getUrl().equals(url)).findFirst();
        return optional.isPresent() ? optional.get().getInnerUrls() : null;
    }

}
