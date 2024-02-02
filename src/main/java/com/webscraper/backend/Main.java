package com.webscraper.backend;

import static spark.Spark.get;
import static spark.Spark.post;

import com.webscraper.ScraperService;
import com.google.gson.Gson;

public class Main {
    public static void main(String[] args) {
        Gson gson = new Gson();
        ScraperService scraperService = new ScraperService();

        get("/crawl/:id", (req, res) -> gson.toJson(scraperService.getScrapedData(req.params("id"))));
        post("/crawl", (req, res) -> gson.toJson(scraperService.crawl(req.body())));
    }
}
