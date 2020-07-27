package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.sps.data.ShawarmaPlace;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Servlet that returns list of shawarma places.
 */
@WebServlet("/shawarma")
public class ShawarmaServlet extends HttpServlet {
    private final String SHAWARMA_FILE = "shawarma.json";
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        try (final Reader reader = Files.newBufferedReader(Paths.get(
                        Thread.currentThread().getContextClassLoader().getResource(SHAWARMA_FILE).toURI()))) {
            ShawarmaPlace[] shawarmaPlaces = gson.fromJson(reader, ShawarmaPlace[].class);
            response.getWriter().println(gson.toJson(shawarmaPlaces));
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
