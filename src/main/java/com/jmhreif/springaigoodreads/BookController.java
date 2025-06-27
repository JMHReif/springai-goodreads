package com.jmhreif.springaigoodreads;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
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
    private final BookRepository bookRepository;

    String prompt = """
            You are a book expert providing recommendations from high-quality book information provided.
            Please summarize the books provided.
            
            PHRASE:
            {searchPhrase}
            """;

    public BookController(ChatClient.Builder builder, Neo4jVectorStore vectorStore, BookRepository repo, BookRepository bookRepository) {
        this.client = builder.build();
        this.vectorStore = vectorStore;
        this.repo = repo;
        this.bookRepository = bookRepository;
    }

    //Retrieval Augmented Generation with Neo4j - vector search + retrieval query for related context
    @GetMapping("/graph")
    public String generateResponseWithContext(@RequestParam String searchPhrase) {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder()
                .query(searchPhrase).topK(5).similarityThreshold(0.8)
                .build());

        List<Book> bookList = repo.findBooks(results.stream().map(Document::getId).toList());
        System.out.println("--- Book list ---");
        System.out.println(bookList);

        var template = new PromptTemplate(prompt).create(Map.of(
                "context", bookList.stream().map(b -> b.toString()).collect(Collectors.joining("\n")),
                "searchPhrase", searchPhrase));
        System.out.println("----- PROMPT -----");
        System.out.println(template);

        return client.prompt(template).call().content();

    }

    //Retrieval Augmented Generation with Neo4j - vector search + retrieval query for related context
    @GetMapping("/graphAdvisor")
    public String generateResponseWithContext2(@RequestParam String searchPhrase) {
        var template = new PromptTemplate(prompt).create(Map.of("searchPhrase", searchPhrase));
        // System.out.println("----- PROMPT -----");
        // System.out.println(template);

        return client.prompt(template)
                .advisors(new SimpleLoggerAdvisor(),
                        new CustomVectorSearchAdvisor(vectorStore, searchPhrase),
                        new GraphRetrievalAdvisor(repo))
                .call().content();

    //testing advisors
    @GetMapping("/graph2")
    public String generateResponseWithContext2(@RequestParam String searchPhrase) {
        String prompt2 = """
            You are a book expert providing recommendations from high-quality book information provided.
            Please summarize the books provided for the user's search phrase.
            """;

        return client.prompt(prompt2)
                .advisors(new SimpleLoggerAdvisor(),
                        new QuestionAnswerAdvisor(vectorStore),
                        new GraphRetrievalAdvisor(bookRepository))
                .user(searchPhrase)
                .call().content();
    }
}
