package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import com.google.sps.data.Comments;
import org.junit.Test;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

public class CommentsServletGetTest extends ServletTest {
    private final String commentText = "text";
    private final Gson gson = new Gson();
    private final Instant instant = Instant.ofEpochMilli(1234567890L);

    private void doGetRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CommentsServlet servlet = new CommentsServlet();
        servlet.init();
        servlet.doGet(request, response);
    }

    @Test
    public void testSingleComment() throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Entity commentEntity = new Entity(Comments.COMMENT_ENTITY_KIND);
        commentEntity.setProperty(Comments.TEXT, commentText);
        commentEntity.setProperty(Comments.DATE, Date.from(instant));
        commentEntity.setProperty(Comments.RATING, 0L);

        datastore.put(commentEntity);
        when(request.getParameter("type")).thenReturn("date");
        when(request.getParameter("quantity")).thenReturn("all");

        doGetRequest(request, response);
        writer.flush();
        String result = stringWriter.toString();

        List<Map.Entry<Long, Comments.Comment>> expected =
                Arrays.asList(new AbstractMap.SimpleEntry(
                        commentEntity.getKey().getId(),
                        Comments.Comment.getCommentFromEntity(commentEntity)));

        assertThat(result).isEqualTo(gson.toJson(expected));
    }

    @Test
    public void testCommentOrderByDate() throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Date date = new Date();

        Entity comment1 = new Entity(Comments.COMMENT_ENTITY_KIND);
        comment1.setProperty(Comments.TEXT, "uno");
        comment1.setProperty(Comments.RATING, 0L);
        comment1.setProperty(Comments.DATE, date);
        datastore.put(comment1);

        date = new Date(date.getTime() + 1);
        Entity comment2 = new Entity(Comments.COMMENT_ENTITY_KIND);
        comment2.setProperty(Comments.TEXT, "dos");
        comment2.setProperty(Comments.RATING, 0L);
        comment2.setProperty(Comments.DATE, date);
        datastore.put(comment2);

        date = new Date(date.getTime() + 1);
        Entity comment3 = new Entity(Comments.COMMENT_ENTITY_KIND);
        comment3.setProperty(Comments.TEXT, "tres");
        comment3.setProperty(Comments.RATING, 0L);
        comment3.setProperty(Comments.DATE, date);
        datastore.put(comment3);

        date = new Date(date.getTime() + 1);
        Entity comment4 = new Entity(Comments.COMMENT_ENTITY_KIND);
        comment4.setProperty(Comments.TEXT, "cuatro");
        comment4.setProperty(Comments.RATING, 0L);
        comment4.setProperty(Comments.DATE, date);
        datastore.put(comment4);

        when(request.getParameter("type")).thenReturn("date");
        when(request.getParameter("quantity")).thenReturn("3");

        doGetRequest(request, response);
        writer.flush();
        String result = stringWriter.toString();

        List<Map.Entry<Long, Comments.Comment>> expected =
                Arrays.asList(
                        new AbstractMap.SimpleEntry(
                                comment4.getKey().getId(),
                                Comments.Comment.getCommentFromEntity(comment4)),
                        new AbstractMap.SimpleEntry(
                                comment3.getKey().getId(),
                                Comments.Comment.getCommentFromEntity(comment3)),
                        new AbstractMap.SimpleEntry(
                                comment2.getKey().getId(),
                                Comments.Comment.getCommentFromEntity(comment2))
                );

        assertThat(result).isEqualTo(gson.toJson(expected));
    }

    @Test
    public void testCommentOrderByRating() throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Date date = new Date();

        Entity comment1 = new Entity(Comments.COMMENT_ENTITY_KIND);
        comment1.setProperty(Comments.TEXT, "uno");
        comment1.setProperty(Comments.RATING, 4L);
        comment1.setProperty(Comments.DATE, date);
        datastore.put(comment1);

        Entity comment2 = new Entity(Comments.COMMENT_ENTITY_KIND);
        comment2.setProperty(Comments.TEXT, "dos");
        comment2.setProperty(Comments.RATING, 2L);
        comment2.setProperty(Comments.DATE, date);
        datastore.put(comment2);

        Entity comment3 = new Entity(Comments.COMMENT_ENTITY_KIND);
        comment3.setProperty(Comments.TEXT, "tres");
        comment3.setProperty(Comments.RATING, 3L);
        comment3.setProperty(Comments.DATE, date);
        datastore.put(comment3);

        Entity comment4 = new Entity(Comments.COMMENT_ENTITY_KIND);
        comment4.setProperty(Comments.TEXT, "cuatro");
        comment4.setProperty(Comments.RATING, 1L);
        comment4.setProperty(Comments.DATE, date);
        datastore.put(comment4);

        when(request.getParameter("type")).thenReturn("rating");
        when(request.getParameter("quantity")).thenReturn("3");

        doGetRequest(request, response);
        writer.flush();
        String result = stringWriter.toString();

        List<Map.Entry<Long, Comments.Comment>> expected =
                Arrays.asList(
                        new AbstractMap.SimpleEntry(
                                comment1.getKey().getId(),
                                Comments.Comment.getCommentFromEntity(comment1)),
                        new AbstractMap.SimpleEntry(
                                comment3.getKey().getId(),
                                Comments.Comment.getCommentFromEntity(comment3)),
                        new AbstractMap.SimpleEntry(
                                comment2.getKey().getId(),
                                Comments.Comment.getCommentFromEntity(comment2))
                );

        assertThat(result).isEqualTo(gson.toJson(expected));
    }
}
