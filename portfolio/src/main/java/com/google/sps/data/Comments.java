package com.google.sps.data;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query;
import java.util.*;


public class Comments {
    public static class Comment {
        private Date date;
        private String text;
        private long rating;

        public Comment(Date date, String text, long rating) {
            this.date = date;
            this.text = text;
            this.rating = rating;
        }

        public Date getDate() {
            return date;
        }

        public long getRating() {
            return rating;
        }

        public String getText() {
            return text;
        }
    }

    private DatastoreService datastore;

    public Comments() {
        this.datastore = DatastoreServiceFactory.getDatastoreService();
    }

    public void addComment(String text) {
        Entity commentEntity = new Entity("Comment");
        commentEntity.setProperty("date", new Date());
        commentEntity.setProperty("text", text);
        commentEntity.setProperty("rating", 0);

        datastore.put(commentEntity);
    }

    private Comment getCommentFromEntity(Entity entity) {
        Date date = (Date) entity.getProperty("date");
        String text = (String) entity.getProperty("text");
        long rating = (long) entity.getProperty("rating");
        return new Comment(date, text, rating);
    }

    public Comment getComment(long id) {
        try {
            Entity commentEntity = getCommentEntity(id);
            return getCommentFromEntity(commentEntity);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    public void deleteComment(long id) {
        Key commentEntityKey = KeyFactory.createKey("Comment", id);
        this.datastore.delete(commentEntityKey);
    }

    private ArrayList<Map.Entry<Long, Comment>> modifyWithQuery(Query query) {
        ArrayList<Map.Entry<Long, Comment>> result = new ArrayList<>();
        PreparedQuery commentEntities = this.datastore.prepare(query);

        for (Entity entity : commentEntities.asIterable()) {
            long id = entity.getKey().getId();
            Comment comment = getCommentFromEntity(entity);
            result.add(new AbstractMap.SimpleEntry(id, comment));
        }

        return result;
    }

    public ArrayList<Map.Entry<Long, Comment>> sortByDate() {
        Query query = new Query("Comment").addSort("date", Query.SortDirection.DESCENDING);
        return modifyWithQuery(query);
    }

    public ArrayList<Map.Entry<Long, Comment>> sortByRating() {
        Query query = new Query("Comment").addSort("rating", Query.SortDirection.DESCENDING);
        return modifyWithQuery(query);
    }

    private Entity getCommentEntity(long id) throws EntityNotFoundException {
        Key commentEntityKey = KeyFactory.createKey("Comment", id);
        return this.datastore.get(commentEntityKey);
    }

    public void upvoteComment(long id) {
        try {
            Entity commentEntity = getCommentEntity(id);
            long rating = (long) commentEntity.getProperty("rating");
            commentEntity.setProperty("rating", rating + 1);
            this.datastore.put(commentEntity);
        } catch (EntityNotFoundException ignored) {}
    }

    public void downvoteComment(long id) {
        try {
            Entity commentEntity = getCommentEntity(id);
            long rating = (long) commentEntity.getProperty("rating");
            commentEntity.setProperty("rating", rating - 1);
            this.datastore.put(commentEntity);
        } catch (EntityNotFoundException ignored) {}
    }
}