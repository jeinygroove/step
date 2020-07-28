package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.sps.data.ShawarmaPlace;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Servlet that returns a list of shawarma places.
 */
@WebServlet("/shawarma")
public class ShawarmaServlet extends HttpServlet {
    private final String SHAWARMA_FILE = "shawarma.json";
    private final Path PATH_SHAWARMA_FILE = Paths.get("WEB-INF", "classes", SHAWARMA_FILE).toAbsolutePath();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        try (final Reader reader = Files.newBufferedReader(PATH_SHAWARMA_FILE)) {
            ShawarmaPlace[] shawarmaPlaces = gson.fromJson(reader, ShawarmaPlace[].class);
            response.getWriter().println(gson.toJson(shawarmaPlaces));
        } catch (JsonIOException | JsonSyntaxException jsonException){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Json file with shawarma places is invalid");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}