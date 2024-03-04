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
public class BookController {
    private final OpenAiChatClient client;
    private final Neo4jVectorStore vectorStore;
    private final BookRepository repo;

    String prompt = """
                You are providing book recommendations for books with reviews containing the searched phrase.
                Always respond with information from the CONTEXT section below.
                Do not add extra information from any outside sources.
                List each book title from the CONTEXT section below.
                Provide as much information as possible from the CONTEXT section, but nothing more.
                If you are unsure about a book, go ahead and list the title and add that you are unsure.
                
                CONTEXT:
                {context}
                
                QUESTION:
                Could you provide some book recommendations containing {searchPhrase}?
                """;

    public BookController(OpenAiChatClient client, Neo4jVectorStore vectorStore, BookRepository repo) {
        this.client = client;
        this.vectorStore = vectorStore;
        this.repo = repo;
    }

    //Test call for generic call to the LLM
    @GetMapping("/hello")
    public String helloAIWorld(@RequestParam(defaultValue = "What is the history of the violin?") String question) {
        return client.call(question);
    }

    //Provide prompt to LLM for book recommendations
    @GetMapping("/llm")
    public String generateLLMResponse(@RequestParam String searchPhrase) {

        var template = new PromptTemplate(prompt, Map.of("context", "", "searchPhrase", searchPhrase));
        System.out.println("----- PROMPT -----");
        System.out.println(template.render());

        return client.call(template.create().getContents());
    }

    //Vector similarity search ONLY! Not valuable here because embeddings are on Review text, not books
    @GetMapping("/vector")
    public String generateSimilarityResponse(@RequestParam String searchPhrase) {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.query(searchPhrase).withTopK(10));

        var template = new PromptTemplate(prompt, Map.of("context", results, "searchPhrase", searchPhrase));
        System.out.println("----- PROMPT -----");
        System.out.println(template.render());

        return client.call(template.create().getContents());
    }

    //Retrieval Augmented Generation with Neo4j - vector search + retrieval query for related context
    @GetMapping("/rag")
    public String generateResponseWithContext(@RequestParam String searchPhrase) {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.query(searchPhrase).withTopK(10));

        List<Book> reviewList = repo.findBooks(results.stream().map(Document::getId).collect(Collectors.toList()));

        var template = new PromptTemplate(prompt, Map.of("context", reviewList, "searchPhrase", searchPhrase));
        System.out.println("----- PROMPT -----");
        System.out.println(template.render());

        return client.call(template.create().getContents());

    }
}
