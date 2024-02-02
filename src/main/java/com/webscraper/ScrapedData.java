package com.webscraper;

import java.util.HashSet;
import java.util.Set;

public class ScrapedData {
    String id;
    String status;
    Set<String> urls;

    ScrapedData(String id) {
        this.id = id;
        this.status = "active";
        this.urls = new HashSet<>();
    }

    ScrapedData(String id, Set<String> urls) {
        this.id = id;
        this.status = "active";
        this.urls = urls;
    }

    public String getId() {
        return id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void addUrl(String url) {
        urls.add(url);
    }

    public Set<String> getUrls() {
        return urls;
    }
}
