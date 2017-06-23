package com.example.q_municate_chat_service.dao;


import android.arch.persistence.room.TypeConverter;
import android.text.TextUtils;

import org.jivesoftware.smack.roster.packet.RosterPacket;

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
    public static RosterPacket.ItemType from(String value) {
        return RosterPacket.ItemType.valueOf(value);
    }

    @TypeConverter
    public static String to(RosterPacket.ItemType value) {
        return value.toString();
    }

    @TypeConverter
    public static RosterPacket.ItemStatus fromStatus(String value) {
        return RosterPacket.ItemStatus.valueOf(value);
    }

    @TypeConverter
    public static String toStatus(RosterPacket.ItemStatus value) {
        return value.toString();
    }

    @TypeConverter
    public static String listToArray(List<Integer> integerList) {

        String listString = TextUtils.join(", ", integerList);
        return listString;
    }

}
