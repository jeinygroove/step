package com.google.sps.data;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class UserManager {
    public static String getCurrentUserId() {
        UserService userService = UserServiceFactory.getUserService();

        if (!userService.isUserLoggedIn()) {
            return null;
        }

        return userService.getCurrentUser().getUserId();
    }
}
