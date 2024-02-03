package com.webscraper;

import java.util.Set;

public class UrlContent {
    String url;
    Set<String> keywords;
    Set<String> innerUrls;

    public UrlContent() {
    }

    public UrlContent(String url, Set<String> keywords, Set<String> innerUrls) {
        this.url = url;
        this.keywords = keywords;
        this.innerUrls = innerUrls;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }

    public Set<String> getInnerUrls() {
        return innerUrls;
    }

    public void setInnerUrls(Set<String> innerUrls) {
        this.innerUrls = innerUrls;
    }
}