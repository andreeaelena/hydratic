package com.hydratic.app.storage;

import com.hydratic.app.model.User;

/**
 * Memory Store represented as a singleton.
 * Stores objects that form the state of the app.
 */
public class MemoryStore {

    private static MemoryStore sInstance;

    private User loggedInUser;

    private MemoryStore() {
        // Private constructor
    }

    public static synchronized MemoryStore getInstance() {
        if (sInstance == null) {
            sInstance = new MemoryStore();
        }
        return sInstance;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
