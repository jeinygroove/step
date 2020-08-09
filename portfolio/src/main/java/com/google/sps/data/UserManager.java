package com.google.sps.data;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * Provides method for getting information about current user.
 * More methods can be added in the future.
 * @author Olga Shimanskaia <olgashimanskaia@gmail.com>
 */
public class UserManager {
    public static String getCurrentUserId() {
        UserService userService = UserServiceFactory.getUserService();
        return userService.isUserLoggedIn() ? userService.getCurrentUser().getUserId() : null;
    }
}
