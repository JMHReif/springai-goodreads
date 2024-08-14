package com.jmhreif.springaigoodreads;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
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
public class BookController {
    private final ChatClient client;
    private final Neo4jVectorStore vectorStore;
    private final BookRepository repo;

    String prompt = """
            You are a book expert providing recommendations from high-quality book information in the CONTEXT section.
            Please summarize the books provided in the context section.
                        
            CONTEXT:
            {context}
                        
            PHRASE:
            {searchPhrase}
            """;

    public BookController(ChatClient.Builder builder, Neo4jVectorStore vectorStore, BookRepository repo) {
        this.client = builder.build();
        this.vectorStore = vectorStore;
        this.repo = repo;
    }

    //Test call for generic call to the LLM
    @GetMapping("/hello")
    public String helloAIWorld(@RequestParam(defaultValue = "What is the history of the violin?") String question) {
        return client.prompt().user(question).call().content();
    }

    //Provide prompt to LLM for book recommendations
    @GetMapping("/llm")
    public String generateLLMResponse(@RequestParam String searchPhrase) {

        var template = new PromptTemplate(prompt, Map.of("context", "", "searchPhrase", searchPhrase));
        System.out.println("----- PROMPT -----");
        System.out.println(template.render());

        return client.prompt(template.create()).call().content();
    }

    //Vector similarity search ONLY! Not valuable here because embeddings are on Review text, not books
    @GetMapping("/vector")
    public String generateSimilarityResponse(@RequestParam String searchPhrase) {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.query(searchPhrase).withTopK(10));
        System.out.println("--- Results ---");
        System.out.println(results);

        var template = new PromptTemplate(prompt, Map.of("context", results, "searchPhrase", searchPhrase));
        System.out.println("----- PROMPT -----");
        System.out.println(template.render());

        return client.prompt(template.create()).call().content();
    }

    //Retrieval Augmented Generation with Neo4j - vector search + retrieval query for related context
    @GetMapping("/rag")
    public String generateResponseWithContext(@RequestParam String searchPhrase) {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.query(searchPhrase).withTopK(10));

        List<Book> bookList = repo.findBooks(results.stream().map(Document::getId).collect(Collectors.toList()));
        System.out.println("--- Book list ---");
        System.out.println(bookList);

        var template = new PromptTemplate(prompt, Map.of("context", bookList.stream().map(b -> b.toString()).collect(Collectors.joining("\n")), "searchPhrase", searchPhrase));
        System.out.println("----- PROMPT -----");
        System.out.println(template.render());

        return client.prompt(template.create()).call().content();

    }
}
