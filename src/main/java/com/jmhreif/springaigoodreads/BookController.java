package com.jmhreif.springaigoodreads;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class BookController {
    private final ChatClient client;
    private final Neo4jVectorStore vectorStore;
    private final BookRepository repo;
    private final BookRepository bookRepository;

    String prompt = """
            You are a book expert providing recommendations from high-quality book information provided.
            Please summarize the books provided.
            """;

    public BookController(ChatClient.Builder builder, Neo4jVectorStore vectorStore, BookRepository repo, BookRepository bookRepository) {
        this.client = builder.build();
        this.vectorStore = vectorStore;
        this.repo = repo;
        this.bookRepository = bookRepository;
    }

    //Retrieval Augmented Generation with Neo4j - vector search + retrieval query for related context
    @GetMapping("/graphAdvisor")
    public String generateResponseWithContext2(@RequestParam String searchPhrase) {
        return client.prompt()
                .system(prompt)
                .user(searchPhrase)
                .advisors(new SimpleLoggerAdvisor(),
                        new QuestionAnswerAdvisor(vectorStore),
                        new GraphRetrievalAdvisor(bookRepository))
                .call().content();
    }
}
