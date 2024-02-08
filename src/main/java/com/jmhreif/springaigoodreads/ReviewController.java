package com.jmhreif.springaigoodreads;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.vectorstore.Neo4jVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class ReviewController {
    private final OpenAiChatClient client;
    private final Neo4jVectorStore vectorStore;
    private final ReviewRepository repo;

    public ReviewController(OpenAiChatClient client, Neo4jVectorStore vectorStore, ReviewRepository repo) {
        this.client = client;
        this.vectorStore = vectorStore;
        this.repo = repo;
    }

    @GetMapping("/llm")
    public String generateResponse(@RequestParam(defaultValue = "What is the history of the violin?") String question) {
        return client.call(question);
    }

    @GetMapping("/rag")
    public String generateResponseWithContext(@RequestParam String searchPhrase) {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.query(searchPhrase).withTopK(10));

        Iterable<Review> reviewList = repo.findBooks(results.stream().map(Document::getId).collect(Collectors.toList()));

        var template = new PromptTemplate("""
                You are providing book recommendations for books with reviews similar to the searched phrase.
                Always respond with information from the CONTEXT section below.
                Do not add titles from external sources.
                If you are not sure about an answer, list the title and say that you are unsure.
                                
                CONTEXT:
                {context}
                                
                QUESTION:
                Could you provide some book recommendations with {searchPhrase}?
                """, Map.of("context", reviewList, "searchPhrase", searchPhrase));
        System.out.println("----- PROMPT -----");
        System.out.println(template.render());

        return client.call(template.create()).getResult().getOutput().getContent();
    }
}
