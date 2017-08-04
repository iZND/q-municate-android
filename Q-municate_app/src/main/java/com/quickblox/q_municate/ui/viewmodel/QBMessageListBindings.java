package com.quickblox.q_municate.ui.viewmodel;


import android.databinding.BindingAdapter;
import android.support.v7.widget.RecyclerView;

import com.example.q_municate_chat_service.entity.QBMessage;

import java.util.List;

public class QBMessageListBindings {

    @SuppressWarnings("unchecked")
    @BindingAdapter("app:items")
    public static void setItems(RecyclerView recyclerView, List<QBMessage> items) {

    }
}
