package com.google.sps.servlets;

import com.google.appengine.api.datastore.*;
import com.google.sps.data.Comments;
import com.google.sps.data.User;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

public class CommentsServletPostTest extends ServletTest {
    private final String commentsPage = "/comments.html";
    private final String commentText = "text";
    private final Clock testClock = Clock.fixed(
            Instant.parse("2018-08-22T10:00:00Z"),
            ZoneOffset.UTC);

    private void doPostRequest(HttpServletRequest request, HttpServletResponse response, Clock testClock) throws IOException {
        CommentsServlet servlet = testClock == null ? new CommentsServlet() : new CommentsServlet(testClock);
        servlet.init();
        servlet.doPost(request, response);
    }

    @Test
    public void testAddComment() throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String currentUserID = User.getCurrentUserId();

        when(request.getParameter("action")).thenReturn("add");
        when(request.getParameter(Comments.TEXT)).thenReturn(commentText);

        doPostRequest(request, response, testClock);
        verify(response).sendRedirect(commentsPage);

        Query query = new Query(Comments.COMMENT_ENTITY_KIND);
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

        Entity expected = new Entity(Comments.COMMENT_ENTITY_KIND);
        expected.setProperty(Comments.DATE, Date.from(testClock.instant()));
        expected.setProperty(Comments.AUTHOR_ID, currentUserID);
        expected.setProperty(Comments.TEXT, commentText);
        expected.setProperty(Comments.RATING, 0L);

        Set<Comments.Comment> resultComments = results.stream().map(
                Comments.Comment::getCommentFromEntity).collect(Collectors.toSet());
        Comments.Comment expectedComment = Comments.Comment.getCommentFromEntity(expected);

        assertThat(resultComments.size()).isEqualTo(1);
        assertThat(resultComments).contains(expectedComment);
    }

    @Test
    public void testAddCommentWithoutText() throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        when(request.getParameter("action")).thenReturn("add");

        doPostRequest(request, response, null);
        verify(response).sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Parameter 'text' doesn't exist, required for action 'add'");

        Query query = new Query(Comments.COMMENT_ENTITY_KIND);
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        assertThat(results).isEmpty();
    }

    @Test
    public void testNonExistingAction() throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        when(request.getParameter("action")).thenReturn("sing");
        new CommentsServlet().doPost(request, response);
        verify(response).sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Parameter 'action' doesn't exist or is invalid");

        Query query = new Query(Comments.COMMENT_ENTITY_KIND);
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        assertThat(results).isEmpty();
    }

    @Test
    public void testDeleteComment() throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String currentUserID = User.getCurrentUserId();

        Entity comment = new Entity(Comments.COMMENT_ENTITY_KIND);
        comment.setProperty(Comments.DATE, new Date());
        comment.setProperty(Comments.AUTHOR_ID, currentUserID);
        comment.setProperty(Comments.TEXT, commentText);
        comment.setProperty(Comments.RATING, 0L);

        datastore.put(comment);

        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("comment-id")).thenReturn(Long.toString(comment.getKey().getId()));

        doPostRequest(request, response, null);
        verify(response).sendRedirect(commentsPage);

        Query query = new Query(Comments.COMMENT_ENTITY_KIND);
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        assertThat(results).isEmpty();
    }

    @Test
    public void testDeleteOtherUserComment() throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String currentUserID = User.getCurrentUserId();

        Entity comment = new Entity(Comments.COMMENT_ENTITY_KIND);
        comment.setProperty(Comments.DATE, new Date());
        comment.setProperty(Comments.AUTHOR_ID, currentUserID + "_");
        comment.setProperty(Comments.TEXT, commentText);
        comment.setProperty(Comments.RATING, 0L);

        datastore.put(comment);

        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("comment-id")).thenReturn(Long.toString(comment.getKey().getId()));

        doPostRequest(request, response, null);
        verify(response).sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "This user isn't allowed to delete the comment, because he isn't the author"
        );

        Query query = new Query(Comments.COMMENT_ENTITY_KIND);
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        assertThat(results.size()).isEqualTo(1);
    }

    @Test
    public void testDeleteNonExistingComment() throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("comment-id")).thenReturn("1");

        doPostRequest(request, response, null);
        verify(response).sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "Comment with this id doesn't exist");

        Query query = new Query(Comments.COMMENT_ENTITY_KIND);
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        assertThat(results).isEmpty();
    }

    @Test
    public void testActionWithoutCommentID() throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        when(request.getParameter("action")).thenReturn("delete");

        doPostRequest(request, response, null);
        verify(response).sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Parameter 'comment-id' isn't a number or doesn't exist");
    }

    private void testVoteComment(Boolean isUpvote) throws IOException {
        final String commentText = "some comment";
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String currentUserID = User.getCurrentUserId();

        Entity comment = new Entity(Comments.COMMENT_ENTITY_KIND);
        comment.setProperty(Comments.DATE, Date.from(testClock.instant()));
        comment.setProperty(Comments.AUTHOR_ID, currentUserID);
        comment.setProperty(Comments.TEXT, commentText);
        comment.setProperty(Comments.RATING, 0L);

        datastore.put(comment);

        when(request.getParameter("action")).thenReturn("vote");
        when(request.getParameter("comment-id")).thenReturn(Long.toString(comment.getKey().getId()));
        when(request.getParameter("isUpvote")).thenReturn(Boolean.toString(isUpvote));

        doPostRequest(request, response, testClock);
        verify(response).sendRedirect(commentsPage);

        Query query = new Query(Comments.COMMENT_ENTITY_KIND);
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

        Entity expected = new Entity(Comments.COMMENT_ENTITY_KIND);
        expected.setProperty(Comments.DATE, Date.from(testClock.instant()));
        expected.setProperty(Comments.AUTHOR_ID, currentUserID);
        expected.setProperty(Comments.TEXT, commentText);
        if (isUpvote) {
            expected.setProperty(Comments.RATING, 1L);
        } else {
            expected.setProperty(Comments.RATING, -1L);
        }

        Set<Comments.Comment> resultComments = results.stream().map(
                Comments.Comment::getCommentFromEntity).collect(Collectors.toSet());
        Comments.Comment expectedComment = Comments.Comment.getCommentFromEntity(expected);

        assertThat(resultComments.size()).isEqualTo(1);
        assertThat(resultComments).contains(expectedComment);
    }

    @Test
    public void testUpvoteComment() throws IOException {
        testVoteComment(true);
    }

    @Test
    public void testDownvoteComment() throws IOException {
        testVoteComment(false);
    }

    @Test
    public void testVoteCommentWithoutIsUpvoteParameter() throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        when(request.getParameter("action")).thenReturn("vote");
        when(request.getParameter("comment-id")).thenReturn("1");

        doPostRequest(request, response, null);
        verify(response).sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Parameter 'isUpvote' isn't boolean or doesn't exist");
    }
}
