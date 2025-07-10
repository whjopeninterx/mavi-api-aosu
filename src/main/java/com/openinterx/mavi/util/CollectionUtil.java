package com.openinterx.mavi.util;

import java.util.Collection;

public class CollectionUtil {

    //判断集合是否为空，空返回true
    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    //判断集合不为空返回true
    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }

}
