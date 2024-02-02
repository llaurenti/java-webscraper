package com.webscraper;

public class InvalidKeywordLengthException extends Exception{

    InvalidKeywordLengthException() {
        super("The keyword must be between 4 and 32 characters long");
    }
}
