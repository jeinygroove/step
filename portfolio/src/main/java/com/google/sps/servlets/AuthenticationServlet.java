package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.UserManager;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet that returns a User if he's logged in
 * and returns loginUrl if not.
 */
@WebServlet("/auth")
public class AuthenticationServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/json");

        UserService userService = UserServiceFactory.getUserService();

        Map<String, Object> res = new HashMap<>();

        if (userService.isUserLoggedIn()) {
            String logoutUrl = userService.createLogoutURL("/");
            String id = UserManager.getCurrentUserId();

            res.put("isLoggedIn", true);
            res.put("id", id);
            res.put("logoutUrl", logoutUrl);
        } else {
            String loginUrl = userService.createLoginURL("/");

            res.put("isLoggedIn", false);
            res.put("loginUrl", loginUrl);
        }

        response.getWriter().println(gson.toJson(res));
    }
}
