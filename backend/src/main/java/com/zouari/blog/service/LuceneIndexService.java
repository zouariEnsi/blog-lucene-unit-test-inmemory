package com.zouari.blog.service;

import com.zouari.blog.model.User;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class LuceneIndexService {
    private static final Logger LOGGER = Logger.getLogger(LuceneIndexService.class.getName());
    private static final String INDEX_DIR = System.getProperty("user.home") + "/lucene-index";
    
    private FSDirectory directory;
    private StandardAnalyzer analyzer;

    @PostConstruct
    public void init() {
        try {
            Path indexPath = Paths.get(INDEX_DIR);
            Files.createDirectories(indexPath);
            this.directory = FSDirectory.open(indexPath);
            this.analyzer = new StandardAnalyzer();
            LOGGER.info("Lucene index initialized at: " + INDEX_DIR);
        } catch (IOException e) {
            LOGGER.severe("Failed to initialize Lucene index: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Lucene index", e);
        }
    }

    public synchronized void indexUsers(List<User> users) throws IOException {
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
