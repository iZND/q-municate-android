package com.example.q_municate_chat_service.entity;


import java.util.List;

public class PagedResult<T> {

    private int currentPage;
    private int perPage;
    private int totalEntries;

    private boolean fromDb;

    private List<T> values;

    public PagedResult(int perPage, int totalEntries, boolean fromDb, List<T> values) {
        this.perPage = perPage;
        this.totalEntries = totalEntries;
        this.fromDb = fromDb;
        this.values = values;
    }

    public PagedResult( boolean fromDb, List<T> values) {
        this.totalEntries = values.size();
        this.fromDb = fromDb;
        this.values = values;
    }


    public int getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(int totalEntries) {
        this.totalEntries = totalEntries;
    }

    public List<T> getValues() {
        return values;
    }
}
