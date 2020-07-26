package com.google.sps.servlets;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Servlet that returns list of shawarma places.
 */
@WebServlet("/shawarma")
public class ShawarmaServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("shawarma.json");
            Scanner scanner = new Scanner(inputStream);
            StringBuffer stringBuffer = new StringBuffer();
            while (scanner.hasNext()) {
                stringBuffer.append(scanner.nextLine());
            }
            response.getWriter().println(stringBuffer.toString());
        } catch(Exception e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
