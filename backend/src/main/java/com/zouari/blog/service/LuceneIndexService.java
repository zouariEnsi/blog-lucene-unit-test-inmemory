package com.zouari.blog.service;

import com.zouari.blog.model.User;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class LuceneIndexService {
    private static final Logger LOGGER = Logger.getLogger(LuceneIndexService.class.getName());
    private static final String INDEX_DIR = System.getProperty("java.io.tmpdir") + "/lucene-index";

    private FSDirectory directory;
    private Analyzer analyzer;
    private volatile boolean initialized = false;

    private synchronized void ensureInitialized() {
        if (!initialized) {
            try {
                Path indexPath = Paths.get(INDEX_DIR);
                Files.createDirectories(indexPath);
                this.directory = FSDirectory.open(indexPath);
                this.analyzer = createAnalyzer();
                this.initialized = true;
                LOGGER.info("Lucene index initialized at: " + INDEX_DIR);
            } catch (IOException e) {
                LOGGER.severe("Failed to initialize Lucene index: " + e.getMessage());
                throw new RuntimeException("Failed to initialize Lucene index", e);
            }
        }
    }

    private Analyzer createAnalyzer() {
        return new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                StandardTokenizer tokenizer = new StandardTokenizer();
                TokenStream filter = new LowerCaseFilter(tokenizer);
                filter = new ASCIIFoldingFilter(filter);
                return new TokenStreamComponents(tokenizer, filter);
            }
        };
    }

    public synchronized void indexUsers(List<User> users) throws IOException {
        ensureInitialized();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            for (User user : users) {
                Document doc = createDocument(user);
                writer.addDocument(doc);
            }
            writer.commit();
            LOGGER.info("Indexed " + users.size() + " users");
        }
    }

    public synchronized void clearIndex() throws IOException {
        ensureInitialized();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            writer.deleteAll();
            writer.commit();
            LOGGER.info("Index cleared");
        }
    }

    private Document createDocument(User user) {
        Document doc = new Document();
        
        if (user.getLogin() != null && user.getLogin().getUuid() != null) {
            doc.add(new StringField("uuid", user.getLogin().getUuid(), Field.Store.YES));
        }
        
        if (user.getLogin() != null && user.getLogin().getUsername() != null) {
            doc.add(new StringField("username", user.getLogin().getUsername(), Field.Store.YES));
        }
        
        if (user.getEmail() != null) {
            doc.add(new StringField("email", user.getEmail(), Field.Store.YES));
        }
        
        if (user.getName() != null) {
            if (user.getName().getFirst() != null) {
                doc.add(new TextField("firstName", user.getName().getFirst(), Field.Store.YES));
            }
            if (user.getName().getLast() != null) {
                doc.add(new TextField("lastName", user.getName().getLast(), Field.Store.YES));
            }
            String fullName = (user.getName().getFirst() != null ? user.getName().getFirst() : "") + 
                            " " + (user.getName().getLast() != null ? user.getName().getLast() : "");
            doc.add(new TextField("fullName", fullName.trim(), Field.Store.YES));
        }
        
        if (user.getGender() != null) {
            doc.add(new StringField("gender", user.getGender(), Field.Store.YES));
        }
        
        if (user.getPhone() != null) {
            doc.add(new StringField("phone", user.getPhone(), Field.Store.YES));
        }
        
        if (user.getCell() != null) {
            doc.add(new StringField("cell", user.getCell(), Field.Store.YES));
        }
        
        if (user.getNat() != null) {
            doc.add(new StringField("nationality", user.getNat(), Field.Store.YES));
        }
        
        if (user.getLocation() != null) {
            if (user.getLocation().getCity() != null) {
                doc.add(new TextField("city", user.getLocation().getCity(), Field.Store.YES));
            }
            if (user.getLocation().getCountry() != null) {
                doc.add(new TextField("country", user.getLocation().getCountry(), Field.Store.YES));
            }
            if (user.getLocation().getState() != null) {
                doc.add(new TextField("state", user.getLocation().getState(), Field.Store.YES));
            }
        }
        
        return doc;
    }

    public synchronized List<User> searchUsersByName(String name) throws IOException {
        ensureInitialized();
        
        // Check if index exists and has documents
        if (!DirectoryReader.indexExists(directory)) {
            throw new IllegalStateException("Index not created. Please create index first.");
        }
        
        List<User> results = new ArrayList<>();
        
        try (IndexReader reader = DirectoryReader.open(directory)) {
            if (reader.numDocs() == 0) {
                throw new IllegalStateException("Index not created. Please create index first.");
            }
            
            IndexSearcher searcher = new IndexSearcher(reader);
            
            // Create a wildcard query for partial matching
            String queryString = "*" + name.toLowerCase() + "*";
            
            // Search in firstName and lastName fields
            String[] fields = {"firstName", "lastName"};
            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
            parser.setAllowLeadingWildcard(true);
            
            Query query = parser.parse(queryString);
            
            TopDocs topDocs = searcher.search(query, 100); // Limit to 100 results
            
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                User user = convertDocumentToUser(doc);
                results.add(user);
            }
            
            LOGGER.info("Found " + results.size() + " users matching: " + name);
        } catch (Exception e) {
            LOGGER.severe("Error searching users: " + e.getMessage());
            throw new IOException("Error searching users", e);
        }
        
        return results;
    }

    private User convertDocumentToUser(Document doc) {
        User user = new User();
        
        // Set login information
        User.Login login = new User.Login();
        login.setUuid(doc.get("uuid"));
        login.setUsername(doc.get("username"));
        user.setLogin(login);
        
        // Set name information
        User.Name name = new User.Name();
        name.setFirst(doc.get("firstName"));
        name.setLast(doc.get("lastName"));
        user.setName(name);
        
        // Set other fields
        user.setEmail(doc.get("email"));
        user.setGender(doc.get("gender"));
        user.setPhone(doc.get("phone"));
        user.setCell(doc.get("cell"));
        user.setNat(doc.get("nationality"));
        
        // Set location information if available
        String city = doc.get("city");
        String country = doc.get("country");
        String state = doc.get("state");
        
        if (city != null || country != null || state != null) {
            User.Location location = new User.Location();
            location.setCity(city);
            location.setCountry(country);
            location.setState(state);
            user.setLocation(location);
        }
        
        return user;
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (directory != null) {
                directory.close();
            }
            LOGGER.info("Lucene index closed");
        } catch (IOException e) {
            LOGGER.severe("Failed to close Lucene index: " + e.getMessage());
        }
    }
}
