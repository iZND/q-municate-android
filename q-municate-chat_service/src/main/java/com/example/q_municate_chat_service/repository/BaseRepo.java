package com.example.q_municate_chat_service.repository;


import android.arch.lifecycle.LiveData;

import java.util.List;

public interface BaseRepo<T, ID>{

    void create(T event);

    LiveData<List<T>> loadAll();

    LiveData<T> loadById(ID id);

    void delete(T event);
}
