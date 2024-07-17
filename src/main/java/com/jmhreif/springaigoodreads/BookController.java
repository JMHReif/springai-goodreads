package com.jmhreif.springaigoodreads;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
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
    private final OpenAiChatModel chatModel;
    private final Neo4jVectorStore vectorStore;
    private final BookRepository repo;

    String prompt = """
            You are a book expert with high-quality book information in the CONTEXT section.
            Answer with every book title provided in the CONTEXT.
            Do not add extra information from any outside sources.
            If you are unsure about a book, list the book and add that you are unsure.
            
            CONTEXT:
            {context}
            
            PHRASE:
            {searchPhrase}
            """;

    public BookController(OpenAiChatModel chatModel, Neo4jVectorStore vectorStore, BookRepository repo) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
        this.repo = repo;
    }

    //Test call for generic call to the LLM
    @GetMapping("/hello")
    public String helloAIWorld(@RequestParam(defaultValue = "What is the history of the violin?") String question) {
        return chatModel.call(question);
    }

    //Provide prompt to LLM for book recommendations
    @GetMapping("/llm")
    public String generateLLMResponse(@RequestParam String searchPhrase) {

        var template = new PromptTemplate(prompt, Map.of("context", "", "searchPhrase", searchPhrase));
        System.out.println("----- PROMPT -----");
        System.out.println(template.render());

        return chatModel.call(template.create().getContents());
    }

    //Vector similarity search ONLY! Not valuable here because embeddings are on Review text, not books
    @GetMapping("/vector")
    public String generateSimilarityResponse(@RequestParam String searchPhrase) {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.query(searchPhrase).withTopK(5));
        System.out.println("--- Results ---");
        System.out.println(results);

        var template = new PromptTemplate(prompt, Map.of("context", results, "searchPhrase", searchPhrase));
        System.out.println("----- PROMPT -----");
        System.out.println(template.render());

        return chatModel.call(template.create().getContents());
    }

    //Retrieval Augmented Generation with Neo4j - vector search + retrieval query for related context
    @GetMapping("/rag")
    public String generateResponseWithContext(@RequestParam String searchPhrase) {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.query(searchPhrase).withTopK(5).withSimilarityThreshold(0.8));

        List<Book> bookList = repo.findBooks(results.stream().map(Document::getId).collect(Collectors.toList()));
        System.out.println("--- ReviewIds ---");
        System.out.println(bookList);

        var template = new PromptTemplate(prompt, Map.of("context", bookList.stream().map(b -> b.toString()).collect(Collectors.joining("\n")), "searchPhrase", searchPhrase));
        System.out.println("----- PROMPT -----");
        System.out.println(template.render());

        return chatModel.call(template.create().getContents());

    }
}
