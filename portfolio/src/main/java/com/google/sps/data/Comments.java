package com.google.sps.data;

import java.util.*;


public class Comments {
    public static class Comment {
        private Date date;
        private String text;
        private int rating;

        public Comment(String text) {
            this.date = new Date();
            this.text = text;
            this.rating = 0;
        }

        public Date getDate() {
            return this.date;
        }

        public String getText() {
            return this.text;
        }

        public int getRating() {
            return this.rating;
        }

        public void upvote() {
            this.rating++;
        }

        public void downvote() {
            this.rating--;
        }
    }

    private HashMap<UUID, Comment> comments;

    public Comments() {
        this.comments = new HashMap<>();
    }

    public void addComment(String text) {
        this.comments.put(UUID.randomUUID(), new Comment(text));
    }

    public Comment getComment(UUID id) {
        return this.comments.get(id);
    }

    public void deleteComment(UUID id) {
        this.comments.remove(id);
    }

    private ArrayList<Map.Entry<UUID, Comment>> sortBy(Comparator<Map.Entry<UUID, Comment>> comparator) {
        // Create a list from elements of HashMap
        List<Map.Entry<UUID, Comment>> list =
                new LinkedList<>(comments.entrySet());

        // Sort the list
        Collections.sort(list, comparator);

        // Put data from sorted list to hashmap
        ArrayList<Map.Entry<UUID, Comment>> temp = new ArrayList<>();
        for (Map.Entry<UUID, Comment> aa : list) {
            temp.add(new AbstractMap.SimpleEntry(aa.getKey(), aa.getValue()));
        }

        return temp;
    }

    public ArrayList<Map.Entry<UUID, Comment>> sortByDate() {
        return sortBy(Comparator.comparing((Map.Entry<UUID, Comment> o) -> (o.getValue().getDate())).reversed());
    }

    public ArrayList<Map.Entry<UUID, Comment>> sortByRating() {
        return sortBy(Comparator.comparingInt((Map.Entry<UUID, Comment> o) -> o.getValue().getRating()).reversed());
    }

    public void upvoteComment(UUID id) {
        this.comments.get(id).upvote();
    }

    public void downvoteComment(UUID id) {
        this.comments.get(id).downvote();
    }
}