package com.zouari.blog.service;

import com.zouari.blog.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleSearchServiceTest {

    private SimpleSearchService simpleSearchService;

    @BeforeEach
    void setUp() {
        simpleSearchService = new SimpleSearchService();
    }

    @Test
    void testSearchUsersWithEmptyCache() {
        List<User> results = simpleSearchService.searchUsersByName("john");
        assertTrue(results.isEmpty());
    }

    @Test
    void testStoreAndSearchUsers() {
        List<User> users = createTestUsers();
        simpleSearchService.storeUsers(users);
        
        assertEquals(4, simpleSearchService.getUserCount());
        
        List<User> results = simpleSearchService.searchUsersByName("john");
        assertFalse(results.isEmpty());
        assertEquals(2, results.size());
    }

    @Test
    void testSearchCaseInsensitive() {
        List<User> users = createTestUsers();
        simpleSearchService.storeUsers(users);
        
        List<User> resultsLower = simpleSearchService.searchUsersByName("john");
        List<User> resultsUpper = simpleSearchService.searchUsersByName("JOHN");
        List<User> resultsMixed = simpleSearchService.searchUsersByName("JoHn");
        
        assertEquals(resultsLower.size(), resultsUpper.size());
        assertEquals(resultsLower.size(), resultsMixed.size());
    }

    @Test
    void testSearchPartialMatch() {
        List<User> users = createTestUsers();
        simpleSearchService.storeUsers(users);
        
        // Search for "mit" should match "Smith"
        List<User> results = simpleSearchService.searchUsersByName("mit");
        
        assertFalse(results.isEmpty());
        boolean foundSmith = results.stream()
            .anyMatch(u -> u.getName() != null && 
                          u.getName().getLast() != null && 
                          u.getName().getLast().contains("Smith"));
        assertTrue(foundSmith);
    }

    @Test
    void testSearchByFirstName() {
        List<User> users = createTestUsers();
        simpleSearchService.storeUsers(users);
        
        List<User> results = simpleSearchService.searchUsersByName("jane");
        
        assertFalse(results.isEmpty());
        boolean foundJane = results.stream()
            .anyMatch(u -> u.getName() != null && 
                          u.getName().getFirst() != null && 
                          u.getName().getFirst().toLowerCase().contains("jane"));
        assertTrue(foundJane);
    }

    @Test
    void testSearchByLastName() {
        List<User> users = createTestUsers();
        simpleSearchService.storeUsers(users);
        
        List<User> results = simpleSearchService.searchUsersByName("walker");
        
        assertFalse(results.isEmpty());
        boolean foundWalker = results.stream()
            .anyMatch(u -> u.getName() != null && 
                          u.getName().getLast() != null && 
                          u.getName().getLast().toLowerCase().contains("walker"));
        assertTrue(foundWalker);
    }

    @Test
    void testSearchNoResults() {
        List<User> users = createTestUsers();
        simpleSearchService.storeUsers(users);
        
        List<User> results = simpleSearchService.searchUsersByName("XYZ123NotExist");
        
        assertTrue(results.isEmpty());
    }

    @Test
    void testClearUsers() {
        List<User> users = createTestUsers();
        simpleSearchService.storeUsers(users);
        assertEquals(4, simpleSearchService.getUserCount());
        
        simpleSearchService.clearUsers();
        assertEquals(0, simpleSearchService.getUserCount());
        
        List<User> results = simpleSearchService.searchUsersByName("john");
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchWithNullOrEmptyQuery() {
        List<User> users = createTestUsers();
        simpleSearchService.storeUsers(users);
        
        List<User> resultsNull = simpleSearchService.searchUsersByName(null);
        assertTrue(resultsNull.isEmpty());
        
        List<User> resultsEmpty = simpleSearchService.searchUsersByName("");
        assertTrue(resultsEmpty.isEmpty());
        
        List<User> resultsWhitespace = simpleSearchService.searchUsersByName("   ");
        assertTrue(resultsWhitespace.isEmpty());
    }

    private List<User> createTestUsers() {
        List<User> users = new ArrayList<>();
        
        // User 1: John Doe
        User user1 = new User();
        User.Name name1 = new User.Name();
        name1.setFirst("John");
        name1.setLast("Doe");
        user1.setName(name1);
        User.Login login1 = new User.Login();
        login1.setUuid("uuid-1");
        login1.setUsername("johndoe");
        user1.setLogin(login1);
        user1.setEmail("john.doe@example.com");
        users.add(user1);
        
        // User 2: Jane Smith
        User user2 = new User();
        User.Name name2 = new User.Name();
        name2.setFirst("Jane");
        name2.setLast("Smith");
        user2.setName(name2);
        User.Login login2 = new User.Login();
        login2.setUuid("uuid-2");
        login2.setUsername("janesmith");
        user2.setLogin(login2);
        user2.setEmail("jane.smith@example.com");
        users.add(user2);
        
        // User 3: Hans Bröcker (with special characters)
        User user3 = new User();
        User.Name name3 = new User.Name();
        name3.setFirst("Hans");
        name3.setLast("Bröcker");
        user3.setName(name3);
        User.Login login3 = new User.Login();
        login3.setUuid("uuid-3");
        login3.setUsername("hansbrocker");
        user3.setLogin(login3);
        user3.setEmail("hans.brocker@example.com");
        users.add(user3);
        
        // User 4: Johnny Walker
        User user4 = new User();
        User.Name name4 = new User.Name();
        name4.setFirst("Johnny");
        name4.setLast("Walker");
        user4.setName(name4);
        User.Login login4 = new User.Login();
        login4.setUuid("uuid-4");
        login4.setUsername("johnnywalker");
        user4.setLogin(login4);
        user4.setEmail("johnny.walker@example.com");
        users.add(user4);
        
        return users;
    }
}
