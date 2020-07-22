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

import com.google.sps.data.Comments;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
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
    private final Gson gson = new Gson();

    @Override
    public void init() {
        comments = new Comments();
    }

    /**
     * Get method, which sorts the comments based on the 'type' header.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String type = request.getParameter("type");
        String quantity = request.getParameter("quantity");
        try {
            if (Comments.DATE.equals(type)) {
                if ("all".equals(quantity.toLowerCase())) {
                    response.getWriter().println(gson.toJson(comments.sortByDate()));
                } else {
                    int n = Integer.parseInt(quantity);
                    response.getWriter().println(gson.toJson(comments.sortByDate(n)));
                }
            } else if (Comments.RATING.equals(type)) {
                if ("all".equals(quantity.toLowerCase())) {
                    response.getWriter().println(gson.toJson(comments.sortByRating()));
                } else {
                    int n = Integer.parseInt(quantity);
                    response.getWriter().println(gson.toJson(comments.sortByRating(n)));
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Post method, make the action on the comment, based on the parameter 'action'.
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        try {
            switch (action) {
                case "add":
                    String text = request.getParameter(Comments.TEXT);
                    comments.addComment(text);
                    break;
                case "vote": {
                    String isUpvote = request.getParameter("isUpvote");
                    long commentID = Long.parseLong(request.getParameter("comment-id"));
                    if ("true".equals(isUpvote)) {
                        comments.upvoteComment(commentID);
                    } else if ("false".equals(isUpvote)) {
                        comments.downvoteComment(commentID);
                    } else {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    }
                    break;
                }
                case "delete": {
                    long commentID = Long.parseLong(request.getParameter("comment-id"));
                    comments.deleteComment(commentID);
                    break;
                }
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    break;
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        response.sendRedirect("/comments.html");
    }
}
