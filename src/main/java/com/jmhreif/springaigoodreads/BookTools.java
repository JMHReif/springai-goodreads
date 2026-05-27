package com.jmhreif.springaigoodreads;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class BookTools {

    private final Neo4jVectorStore vectorStore;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public BookTools(Neo4jVectorStore vectorStore, BookRepository bookRepository, UserRepository userRepository) {
        this.vectorStore = vectorStore;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Tool(description = "Search for book reviews semantically similar to the given phrase and return their IDs")
    public List<String> searchSimilarReviews(String searchPhrase) {
        return vectorStore.doSimilaritySearch(SearchRequest.builder().query(searchPhrase).build())
                .stream().map(doc -> doc.getId()).toList();
    }

    @Tool(description = "Find books associated with the given review IDs")
    public List<Book> findBooksByReviewIds(List<String> reviewIds) {
        return bookRepository.findBooks(reviewIds);
    }

    @Tool(description = "Add a new book to the database with a title, ISBN, and ISBN-13")
    public Book addBook(String title, String isbn, String isbn13) {
        return bookRepository.save(new Book(UUID.randomUUID().toString(), title, isbn, isbn13, List.of()));
    }

    @Tool(description = "Add a new review for an existing book, identified by bookId")
    public Book addReview(String bookId, String reviewText, int rating) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("No book found with id: " + bookId));
        List<Review> reviews = new ArrayList<>(book.reviewList());
        reviews.add(new Review(UUID.randomUUID().toString(), reviewText, rating));
        return bookRepository.save(new Book(book.book_id(), book.title(), book.isbn(), book.isbn13(), reviews));
    }

    @Tool(description = "Find a user and their published reviews by user ID")
    public User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No user found with id: " + userId));
    }

    @Tool(description = "Find all books that a user has reviewed, identified by user ID")
    public List<Book> findBooksByUser(String userId) {
        return userRepository.findBooksByUser(userId);
    }

    @Tool(description = "Find book recommendations for a user based on what similar users have rated highly, " +
            "excluding books the user has already reviewed")
    public List<Book> findCollaborativeRecommendations(String userId) {
        return userRepository.findCollaborativeRecommendations(userId);
    }
}
