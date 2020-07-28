package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.sps.data.WordsFrequencies;
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
 * Servlet that returns words number of appearances for albums.
 */
@WebServlet("/words")
public class WordFrequenciesServlet extends HttpServlet {
    private final String WORDS_FREQUENCIES_FILE = "wordsFrequencies.json";
    private final Path PATH_WORDS_FREQUENCIES_FILE = Paths.get("WEB-INF", "classes", WORDS_FREQUENCIES_FILE).toAbsolutePath();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        try (final Reader reader = Files.newBufferedReader(PATH_WORDS_FREQUENCIES_FILE)) {
            WordsFrequencies[] wordsFrequencies = gson.fromJson(reader, WordsFrequencies[].class);
            response.getWriter().println(gson.toJson(wordsFrequencies));
        } catch (JsonIOException | JsonSyntaxException e2) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Json file with words frequencies is invalid");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}