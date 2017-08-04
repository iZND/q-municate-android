package com.example.q_municate_chat_service.util;


import com.quickblox.core.server.Performer;
import com.quickblox.extensions.RxJavaPerformProcessor;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class RxUtils {

    public static <T> Observable<T> makeObservable(T result){
        return Observable.defer(() -> Observable.just(result));
    }

    public static <T> Observable<T> makeObservable(Performer<T> performer){
        return (Observable<T>)(performer.convertTo(RxJavaPerformProcessor.INSTANCE));
    }
}
