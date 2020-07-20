// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.gson.JsonObject;
import com.google.sps.data.Comments;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that returns, adds and votes comments
 */
@WebServlet("/comments")
public class CommentsServlet extends HttpServlet {
    private Comments comments;
    private Gson gson = new Gson();

    @Override
    public void init() {
        comments = new Comments();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String type = request.getHeader("type");
        if (type.equals("date")) {
            response.getWriter().println(convertToJson(comments.sortByDate()));
        } else if (type.equals("rating")) {
            response.getWriter().println(convertToJson(comments.sortByRating()));
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String action = request.getParameter("action");
        if (action.equals("add")) {
            String text = request.getParameter("comment-text");
            comments.addComment(text);
        } else if (action.equals("vote")) {
            String isUpvote = request.getParameter("isUpvote");
            UUID commentID = UUID.fromString(request.getParameter("comment-id"));
            if (isUpvote.equals("true")) {
                comments.upvoteComment(commentID);
            } else if (isUpvote.equals("false")) {
                comments.downvoteComment(commentID);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            JsonObject json = new JsonObject();
            json.addProperty("rating", comments.getComment(commentID).getRating());
            response.getWriter().println(json.toString());
            return;
        }
        response.sendRedirect("/comments.html");
    }

    private String convertToJson(ArrayList<Map.Entry<UUID, Comments.Comment>> comments) {
        return gson.toJson(comments);
    }
}
