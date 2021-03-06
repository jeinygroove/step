package com.google.sps.data;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.repackaged.com.google.common.annotations.VisibleForTesting;

import java.util.*;

/**
 * Represents a datastore of comments.
 * @author Olga Shimanskaia <olgashimanskaia@gmail.com>
 */
public class Comments {
    public static final String COMMENT_ENTITY_KIND = "Comment";
    public static final String DATE = "date";
    public static final String RATING = "rating";
    public static final String TEXT = "comment-text";
    public static final String AUTHOR_ID = "author-id";

    /**
     * Represents the comment.
     */
    @VisibleForTesting
    public static class Comment {
        private final Date date;
        private final String text;
        private final String authorID;
        long rating;

        /** Creates the comment with specified fields.
         * @param date      The date of sending a comment.
         * @param text      The comment's text.
         * @param authorID  The comment's author.
         * @param rating    The comment's rating.
         */
        public Comment(Date date, String text, String authorID, long rating) {
            this.date = date;
            this.text = text;
            this.authorID = authorID;
            this.rating = rating;
        }

        public Date getDate() {
            return date;
        }

        public String getText() {
            return text;
        }

        /**
         * Gets the comment from comment entity.
         * @param entity    Entity of the "Comment" kind
         * @return A Comment object with fields retrieved from the entity.
         */
        public static Comment getCommentFromEntity(Entity entity) {
            Date date = (Date) entity.getProperty(DATE);
            String text = (String) entity.getProperty(TEXT);
            String authorID = (String) entity.getProperty(AUTHOR_ID);
            long rating = (long) entity.getProperty(RATING);
            return new Comment(date, text, authorID, rating);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Comment otherComment = (Comment) obj;
            return Objects.equals(this.date, otherComment.date)
                    && Objects.equals(this.text, otherComment.text)
                    && Objects.equals(this.authorID, otherComment.authorID)
                    && this.rating == otherComment.rating;
        }

        //Idea from effective Java : Item 9
        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + Objects.hashCode(date);
            result = 31 * result + Objects.hashCode(text);
            result = 31 * result + Objects.hashCode(authorID);
            result = 31 * result + Long.hashCode(rating);
            return result;
        }
    }

    private final DatastoreService datastore;

    public Comments() {
        this.datastore = DatastoreServiceFactory.getDatastoreService();
    }

    /**
     * Creates the comment with the specified text and puts it to the datastore.
     * @param authorID      The id of the comment's author.
     * @param text          The comment's text.
     * @param date          The comment's date.
     */
    public void addComment(String authorID, String text, Date date) {
        Entity commentEntity = new Entity(COMMENT_ENTITY_KIND);
        commentEntity.setProperty(DATE, date);
        commentEntity.setProperty(TEXT, text);
        commentEntity.setProperty(RATING, 0);
        commentEntity.setProperty(AUTHOR_ID, authorID);

        datastore.put(commentEntity);
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
     * Checks if comment with given id exists or not.
     * @param id    Id of the "Comment" entity
     * @return true, if comment exists, and false if not
     */
    public boolean commentExists(long id) {
        Key commentEntityKey = KeyFactory.createKey(COMMENT_ENTITY_KIND, id);
        Query.Filter keyFilter =
                new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.EQUAL, commentEntityKey);
        Query query = new Query(COMMENT_ENTITY_KIND).setKeysOnly().setFilter(keyFilter);
        List<Entity> result = this.datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        return !result.isEmpty();
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
            Comment comment = Comment.getCommentFromEntity(entity);
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
            Comment comment = Comment.getCommentFromEntity(entity);
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

    /**
     * Returns id of comment's author with the specified id of its' entity
     * or null if entity doesn't exist.
     * @param id    Comments' entity id
     * @return      Author's id or null
     */
    public String getCommentAuthorID(long id) {
        try {
            Entity commentEntity = getCommentEntity(id);
            return (String) commentEntity.getProperty(AUTHOR_ID);
        } catch (EntityNotFoundException ignored) {
            return null;
        }
    }
}