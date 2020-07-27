package com.google.sps.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Servlet that returns words number of appearances for albums.
 */
@WebServlet("/words")
public class WordFrequenciesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("wordsFrequencies.json");
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