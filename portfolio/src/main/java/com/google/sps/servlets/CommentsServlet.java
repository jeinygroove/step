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
        if (type == null || quantity == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missed 'type' or/and 'quantity' parameter");
            return;
        }

        int numberOfComments;
        try {
            numberOfComments = Integer.parseInt(quantity);
        } catch (NumberFormatException e) {
            if ("all".equals(quantity.toLowerCase())) {
                switch (type) {
                    case Comments.DATE: response.getWriter().println(gson.toJson(comments.sortByDate())); break;
                    case Comments.RATING: response.getWriter().println(gson.toJson(comments.sortByRating())); break;
                    default: response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown value of 'type', it must be 'date' or 'rating'");
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter 'quantity' isn't a number and isn't equal to 'all'");
            }
            return;
        }

        switch (type) {
            case Comments.DATE: response.getWriter().println(gson.toJson(comments.sortByDate(numberOfComments))); break;
            case Comments.RATING: response.getWriter().println(gson.toJson(comments.sortByRating(numberOfComments))); break;
            default: response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown value of 'type', it must be 'date' or 'rating'");
        }
    }

    /**
     * Post method, make the action on the comment, based on the parameter 'action'.
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String commentsPage = "/comments.html";
        String action = request.getParameter("action");
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter 'action' doesn't exist");
            return;
        }

        // if we want to add comment, then there's no commentID
        if ("add".equals(action)) {
            String text = request.getParameter(Comments.TEXT);
            if (text == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter 'text' doesn't exist, required for action 'add'");
                return;
            }

            comments.addComment(text);
            response.sendRedirect(commentsPage);
            return;
        }

        // if we want to do smth else with comments, then we also need commentID
        long commentID;
        try {
            commentID = Long.parseLong(request.getParameter("comment-id"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter 'comment-id' isn't a number or doesn't exist");
            return;
        }

        switch (action) {
            case "vote": {
                String isUpvote = request.getParameter("isUpvote");
                if ("true".equals(isUpvote)) {
                    comments.upvoteComment(commentID);
                } else if ("false".equals(isUpvote)) {
                    comments.downvoteComment(commentID);
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter 'isUpvote' isn't boolean or doesn't exist");
                    return;
                }
                break;
            }
            case "delete": comments.deleteComment(commentID); break;
            default: response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown value of parameter 'action'"); return;
        }
        response.sendRedirect(commentsPage);
    }
}
