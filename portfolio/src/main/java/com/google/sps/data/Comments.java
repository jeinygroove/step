package com.google.sps.data;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query;
import java.util.*;

/**
 * Represents a datastore of comments.
 * @author Olga Shimanskaia <olgashimanskaia@gmail.com>
 */
public class Comments {
    private static final String COMMENT_ENTITY_KIND = "Comment";
    public static final String DATE = "date";
    public static final String RATING = "rating";
    public static final String TEXT = "comment-text";
    public static final String IMAGE_URL = "comment-image";

    /**
     * Represents the comment.
     */
    private static class Comment {
        Date date;
        String text;
        long rating;
        String imageURL;

        /** Creates the comment with specified fields.
         * @param date      The date of sending a comment.
         * @param text      The comment's text.
         * @param rating    The comment's rating.
         * @param imageURL  Url to the comments image, if exist.
         */
        public Comment(Date date, String text, long rating, String imageURL) {
            this.date = date;
            this.text = text;
            this.rating = rating;
            this.imageURL = imageURL;
        }
    }

    private final DatastoreService datastore;

    public Comments() {
        this.datastore = DatastoreServiceFactory.getDatastoreService();
    }

    /**
     * Creates the comment with the specified text and puts it to the datastore.
     * @param text      The comment's text.
     * @param imageURL  Url to the comments image, if exist.
     */
    public void addComment(String text, String imageURL) {
        Entity commentEntity = new Entity(COMMENT_ENTITY_KIND);
        commentEntity.setProperty(DATE, new Date());
        commentEntity.setProperty(TEXT, text);
        commentEntity.setProperty(RATING, 0);
        commentEntity.setProperty(IMAGE_URL, imageURL);
        datastore.put(commentEntity);
    }

    /**
     * Gets the comment from comment entity.
     * @param entity    Entity of the "Comment" kind
     * @return A Comment object with fields retrieved from the entity.
     */
    private Comment getCommentFromEntity(Entity entity) {
        Date date = (Date) entity.getProperty(DATE);
        String text = (String) entity.getProperty(TEXT);
        long rating = (long) entity.getProperty(RATING);
        String imageURL = (String) entity.getProperty(IMAGE_URL);
        return new Comment(date, text, rating, imageURL);
    }

    /**
     * Deletes the comment with given id from the datastore.
     * @param id    Id of the "Comment" entity
     */
    public void deleteComment(long id) {
        Key commentEntityKey = KeyFactory.createKey(COMMENT_ENTITY_KIND, id);
        this.datastore.delete(commentEntityKey);
    }

    /**
     * Modifies comments in datastore with query.
     * @param query    Query for datastore.
     * @return List of pairs (Comment id, Comment object), sorted according to the query.
     */
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

    /**
     * Modifies comments in datastore with query and returns first 'numberOfComments'.
     * @param query                   Query for datastore.
     * @param numberOfComments        Number of comments to return.
     * @return List of pairs (Comment id, Comment object), sorted according to the query.
     */
    private ArrayList<Map.Entry<Long, Comment>> modifyWithQuery(Query query, int numberOfComments) {
        ArrayList<Map.Entry<Long, Comment>> result = new ArrayList<>();
        List<Entity> commentEntities = this.datastore.prepare(query).asList(FetchOptions.Builder.withLimit(numberOfComments));

        for (Entity entity : commentEntities) {
            long id = entity.getKey().getId();
            Comment comment = getCommentFromEntity(entity);
            result.add(new AbstractMap.SimpleEntry(id, comment));
        }

        return result;
    }

    /**
     * Sorts comments from datastore by the date in descending order.
     * @return List of pairs (Comment id, Comment object), sorted by the date in descending order.
     */
    public ArrayList<Map.Entry<Long, Comment>> sortByDate() {
        Query query = new Query(COMMENT_ENTITY_KIND).addSort(DATE, Query.SortDirection.DESCENDING);
        return modifyWithQuery(query);
    }

    /**
     * Sorts comments from datastore by the date in descending order and return first 'numberOfComments'.
     * @param numberOfComments        Number of comments to return.
     * @return List of pairs (Comment id, Comment object), sorted by the date in descending order.
     */
    public ArrayList<Map.Entry<Long, Comment>> sortByDate(int numberOfComments) {
        Query query = new Query(COMMENT_ENTITY_KIND).addSort(DATE, Query.SortDirection.DESCENDING);
        return modifyWithQuery(query, numberOfComments);
    }

    /**
     * Sorts comments from datastore by the rating in descending order.
     * @return List of pairs (Comment id, Comment object), sorted by the rating in descending order.
     */
    public ArrayList<Map.Entry<Long, Comment>> sortByRating() {
        Query query = new Query(COMMENT_ENTITY_KIND).addSort(RATING, Query.SortDirection.DESCENDING);
        return modifyWithQuery(query);
    }

    /**
     * Sorts comments from datastore by the rating in descending order and return first 'n'.
     * @param numberOfComments        Number of comments to return.
     * @return List of pairs (Comment id, Comment object), sorted by the rating in descending order.
     */
    public ArrayList<Map.Entry<Long, Comment>> sortByRating(int numberOfComments) {
        Query query = new Query(COMMENT_ENTITY_KIND).addSort(RATING, Query.SortDirection.DESCENDING);
        return modifyWithQuery(query, numberOfComments);
    }

    /**
     * Gets comment with the specified id of its entity.
     * @param id    Comments' entity id
     * @throws EntityNotFoundException  If comment with such id wasn't found in the datastore.
     * @return Comment object with this id.
     */
    private Entity getCommentEntity(long id) throws EntityNotFoundException {
        Key commentEntityKey = KeyFactory.createKey(COMMENT_ENTITY_KIND, id);
        return this.datastore.get(commentEntityKey);
    }

    /**
     * Upvotes comment with the specified id of its' entity.
     * @param id    Comments' entity id
     */
    public void upvoteComment(long id) {
        try {
            Entity commentEntity = getCommentEntity(id);
            long rating = (long) commentEntity.getProperty(RATING);
            commentEntity.setProperty(RATING, rating + 1);
            this.datastore.put(commentEntity);
        } catch (EntityNotFoundException ignored) {}
    }

    /**
     * Downvotes comment with the specified id of its' entity.
     * @param id    Comments' entity id
     */
    public void downvoteComment(long id) {
        try {
            Entity commentEntity = getCommentEntity(id);
            long rating = (long) commentEntity.getProperty(RATING);
            commentEntity.setProperty(RATING, rating - 1);
            this.datastore.put(commentEntity);
        } catch (EntityNotFoundException ignored) {}
    }
}