package com.google.sps;

import com.google.appengine.api.datastore.*;
import com.google.sps.data.Comments;
import com.google.sps.servlets.CommentsServlet;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CommentsServletPostTest extends ServletTest {
    private final String commentsPage = "/comments.html";
    private final String commentText = "text";
    private final Instant instant = Instant.ofEpochMilli(1234567890L);

    private void doPostRequest(HttpServletRequest request, HttpServletResponse response, Instant instant) throws IOException {
        CommentsServlet servlet = new CommentsServlet();
        servlet.init();
        servlet.setMockInstant(instant);
        servlet.doPost(request, response);
    }

    @Test
    public void testAddComment() throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        when(request.getParameter("action")).thenReturn("add");
        when(request.getParameter(Comments.TEXT)).thenReturn(commentText);

        doPostRequest(request, response, instant);
        verify(response).sendRedirect(commentsPage);

        Query query = new Query(Comments.COMMENT_ENTITY_KIND);
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

        Entity expected = new Entity(Comments.COMMENT_ENTITY_KIND);
        expected.setProperty(Comments.DATE, Date.from(instant));
        expected.setProperty(Comments.TEXT, commentText);
        expected.setProperty(Comments.RATING, 0L);

        assertThat(results.stream().map(PropertyContainer::getProperties).toArray(),
                arrayContaining(expected.getProperties()));
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
        assertThat(results, empty());
    }

    @Test
    public void testNonExistingAction() throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        when(request.getParameter("action")).thenReturn("sing");
        new CommentsServlet().doPost(request, response);
        verify(response).sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Parameter 'action' doesn't exist");

        Query query = new Query(Comments.COMMENT_ENTITY_KIND);
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        assertThat(results, empty());
    }

    @Test
    public void testDeleteComment() throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Entity comment = new Entity(Comments.COMMENT_ENTITY_KIND);
        comment.setProperty(Comments.DATE, new Date());
        comment.setProperty(Comments.TEXT, commentText);
        comment.setProperty(Comments.RATING, 0L);

        datastore.put(comment);

        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("comment-id")).thenReturn(Long.toString(comment.getKey().getId()));

        doPostRequest(request, response, null);
        verify(response).sendRedirect(commentsPage);

        Query query = new Query(Comments.COMMENT_ENTITY_KIND);
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        assertThat(results, empty());
    }

    @Test
    public void testDeleteNonExistingComment() throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("comment-id")).thenReturn("1");

        doPostRequest(request, response, null);
        verify(response).sendRedirect(commentsPage);

        Query query = new Query(Comments.COMMENT_ENTITY_KIND);
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        assertThat(results, empty());
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

        Entity comment = new Entity(Comments.COMMENT_ENTITY_KIND);
        comment.setProperty(Comments.DATE, Date.from(instant));
        comment.setProperty(Comments.TEXT, commentText);
        comment.setProperty(Comments.RATING, 0L);

        datastore.put(comment);

        when(request.getParameter("action")).thenReturn("vote");
        when(request.getParameter("comment-id")).thenReturn(Long.toString(comment.getKey().getId()));
        when(request.getParameter("isUpvote")).thenReturn(Boolean.toString(isUpvote));

        doPostRequest(request, response, instant);
        verify(response).sendRedirect(commentsPage);

        Query query = new Query(Comments.COMMENT_ENTITY_KIND);
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

        Entity expected = new Entity(Comments.COMMENT_ENTITY_KIND);
        expected.setProperty(Comments.DATE, Date.from(instant));
        expected.setProperty(Comments.TEXT, commentText);
        if (isUpvote) {
            expected.setProperty(Comments.RATING, 1L);
        } else {
            expected.setProperty(Comments.RATING, -1L);
        }

        assertThat(results.stream().map(PropertyContainer::getProperties).toArray(),
                arrayContaining(expected.getProperties()));
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
