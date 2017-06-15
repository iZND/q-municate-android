package com.example.q_municate_chat_service.dao;


import android.arch.persistence.room.TypeConverter;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateConverter {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static List<Integer> fromList(String value) {
        String[] strings = TextUtils.split(value, ", ");
        List<Integer> intList = new ArrayList<Integer>(strings.length);
        for (int index = 0; index < strings.length; index++)
        {
            intList.add(Integer.valueOf(strings[index]));
        }
        return intList;
    }

    @TypeConverter
    public static String listToArray(List<Integer> integerList) {

        String listString = TextUtils.join(", ", integerList);
        return listString;
    }

}
