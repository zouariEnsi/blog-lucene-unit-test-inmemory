package com.zouari.blog.service;

import com.zouari.blog.model.User;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Simple linear search implementation without Lucene.
 * This serves as a baseline for performance comparison.
 * 
 * This implementation performs a case-insensitive linear search through
 * all users in memory, checking if the search term appears in first name or last name.
 */
@ApplicationScoped
public class SimpleSearchService {
    private static final Logger LOGGER = Logger.getLogger(SimpleSearchService.class.getName());
    
    private volatile List<User> usersCache = new ArrayList<>();
    
    /**
     * Store users in memory for simple search
     */
    public synchronized void storeUsers(List<User> users) {
        this.usersCache = new ArrayList<>(users);
        LOGGER.info("Stored " + users.size() + " users in memory for simple search");
    }
    
    /**
     * Clear the users cache
     */
    public synchronized void clearUsers() {
        this.usersCache.clear();
        LOGGER.info("Cleared users cache");
    }
    
    /**
     * Search users using simple linear iteration.
     * This is O(n) complexity where n is the number of users.
     * 
     * @param name the search term
     * @return list of matching users
     */
    public List<User> searchUsersByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String searchTerm = name.trim().toLowerCase();
        List<User> results = new ArrayList<>();
        
        // Linear search through all users
        for (User user : usersCache) {
            if (user.getName() != null) {
                String firstName = user.getName().getFirst();
                String lastName = user.getName().getLast();
                
                boolean matchesFirstName = firstName != null && 
                    firstName.toLowerCase().contains(searchTerm);
                boolean matchesLastName = lastName != null && 
                    lastName.toLowerCase().contains(searchTerm);
                
                if (matchesFirstName || matchesLastName) {
                    results.add(user);
                }
            }
        }
        
        LOGGER.info("Simple search for '" + name + "' found " + results.size() + " results");
        return results;
    }
    
    /**
     * Get the total number of users in cache
     */
    public int getUserCount() {
        return usersCache.size();
    }
}
