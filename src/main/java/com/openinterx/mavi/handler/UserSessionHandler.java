package com.openinterx.mavi.handler;

import lombok.Getter;
import lombok.Setter;

public class UserSessionHandler {

    private static final ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    public static void setUser(User user) {
        userThreadLocal.set(user);
    }
    public static String getClientId(){
        return userThreadLocal.get().getClientId();
    }

    public static User getUser() {
        return userThreadLocal.get();
    }

    public static void clear() {
        userThreadLocal.remove();
    }

    @Getter
    @Setter
    public static class User{
        private String clientId;
    }
}
