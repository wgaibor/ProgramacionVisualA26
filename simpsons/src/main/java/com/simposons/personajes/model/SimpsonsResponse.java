package com.simposons.personajes.model;

import java.util.List;

public class SimpsonsResponse {

    private int count;
    private String next;
    private String prev;
    private int pages;
    private List<Personaje> results;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrev() {
        return prev;
    }

    public void setPrev(String prev) {
        this.prev = prev;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public List<Personaje> getResults() {
        return results;
    }

    public void setResults(List<Personaje> results) {
        this.results = results;
    }

}