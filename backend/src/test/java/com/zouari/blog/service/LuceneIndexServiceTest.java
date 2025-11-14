package com.zouari.blog.service;

import com.zouari.blog.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LuceneIndexServiceTest {

    private LuceneIndexService luceneIndexService;
    private static final String TEST_INDEX_DIR = System.getProperty("java.io.tmpdir") + "/lucene-index";

    @BeforeEach
    void setUp() throws IOException {
        luceneIndexService = new LuceneIndexService();
        // Clean up any existing index
        cleanupIndex();
    }

    @AfterEach
    void tearDown() throws IOException {
        luceneIndexService.cleanup();
        cleanupIndex();
    }

    private void cleanupIndex() throws IOException {
        Path indexPath = Paths.get(TEST_INDEX_DIR);
        if (Files.exists(indexPath)) {
            Files.walk(indexPath)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        }
    }

    @Test
    void testSearchWithoutIndexShouldThrowException() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            luceneIndexService.searchUsersByName("test");
        });
        assertTrue(exception.getMessage().contains("Index not created"));
    }

    @Test
    void testSearchUsersWithNormalizedText() throws IOException {
        // Create test users with special characters
        List<User> users = createTestUsers();
        
        // Index users
        luceneIndexService.indexUsers(users);
        
        // Search for "Bro" should match "Bröcker"
        List<User> results = luceneIndexService.searchUsersByName("Bro");
        
        assertFalse(results.isEmpty());
        boolean foundBrocker = results.stream()
            .anyMatch(u -> u.getName() != null && 
                          u.getName().getLast() != null && 
                          u.getName().getLast().equals("Bröcker"));
        assertTrue(foundBrocker, "Should find user with last name 'Bröcker' when searching for 'Bro'");
    }

    @Test
    void testSearchUsersCaseInsensitive() throws IOException {
        List<User> users = createTestUsers();
        luceneIndexService.indexUsers(users);
        
        // Search with different cases
        List<User> resultsLower = luceneIndexService.searchUsersByName("john");
        List<User> resultsUpper = luceneIndexService.searchUsersByName("JOHN");
        List<User> resultsMixed = luceneIndexService.searchUsersByName("JoHn");
        
        assertFalse(resultsLower.isEmpty());
        assertEquals(resultsLower.size(), resultsUpper.size());
        assertEquals(resultsLower.size(), resultsMixed.size());
    }

    @Test
    void testSearchUsersByFirstName() throws IOException {
        List<User> users = createTestUsers();
        luceneIndexService.indexUsers(users);
        
        List<User> results = luceneIndexService.searchUsersByName("john");
        
        assertFalse(results.isEmpty());
        boolean foundJohn = results.stream()
            .anyMatch(u -> u.getName() != null && 
                          u.getName().getFirst() != null && 
                          u.getName().getFirst().toLowerCase().contains("john"));
        assertTrue(foundJohn);
    }

    @Test
    void testSearchUsersByLastName() throws IOException {
        List<User> users = createTestUsers();
        luceneIndexService.indexUsers(users);
        
        List<User> results = luceneIndexService.searchUsersByName("doe");
        
        assertFalse(results.isEmpty());
        boolean foundDoe = results.stream()
            .anyMatch(u -> u.getName() != null && 
                          u.getName().getLast() != null && 
                          u.getName().getLast().toLowerCase().contains("doe"));
        assertTrue(foundDoe);
    }

    @Test
    void testSearchUsersPartialMatch() throws IOException {
        List<User> users = createTestUsers();
        luceneIndexService.indexUsers(users);
        
        // Search for "mit" should match "Smith"
        List<User> results = luceneIndexService.searchUsersByName("mit");
        
        assertFalse(results.isEmpty());
        boolean foundSmith = results.stream()
            .anyMatch(u -> u.getName() != null && 
                          u.getName().getLast() != null && 
                          u.getName().getLast().contains("Smith"));
        assertTrue(foundSmith);
    }

    @Test
    void testSearchUsersNoResults() throws IOException {
        List<User> users = createTestUsers();
        luceneIndexService.indexUsers(users);
        
        List<User> results = luceneIndexService.searchUsersByName("XYZ123NotExist");
        
        assertTrue(results.isEmpty());
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
        
        // User 3: With special characters (Bröcker)
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
        
        // User 4: Another John
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
