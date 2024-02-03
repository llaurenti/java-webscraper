package com.webscraper;

public class ScrapeRunnable implements Runnable {
    private ScraperService s;
    private String id;
    private String keyword;

    public ScrapeRunnable(ScraperService s, String id, String keyword) {
        this.s = s;
        this.id = id;
        this.keyword = keyword;
    }

    @Override
    public void run() {
        s.scrape(id, keyword);
    }

}
