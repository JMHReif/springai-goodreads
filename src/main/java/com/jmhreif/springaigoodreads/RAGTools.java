package com.jmhreif.springaigoodreads;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;

import java.util.List;
import java.util.stream.Collectors;

public class RAGTools {
    private final Neo4jVectorStore vectorStore;
    private final BookRepository repo;

    public RAGTools(Neo4jVectorStore vectorStore, BookRepository repo) {
        this.vectorStore = vectorStore;
        this.repo = repo;
    }


    @Tool(description = "Find recommendations based on the user's searchPhrase")
    List<String> getSimilarDocuments(String searchPhrase) {
        return vectorStore.similaritySearch(searchPhrase)
                .stream().map(Document::getId).collect(Collectors.toList());
    }

    @Tool(description = "Find related books for recommendations to the user's search phrase")
    List<Book> getRelatedBooks(List<String> similarDocIds) {
        //Map JSON string back into list of reviewId strings
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> results = objectMapper.convertValue(similarDocIds, new TypeReference<>() {});

        return repo.findBooks(results);
    }
}
