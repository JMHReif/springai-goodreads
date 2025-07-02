package com.jmhreif.springaigoodreads;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/")
public class BookController {
    private final ChatClient client;
    private final RAGTools ragTools;

    String prompt = """
            You are a book expert providing recommendations from high-quality book information in the CONTEXT section.
            Please summarize the books provided in the context section.
            
            CONTEXT:
            {context}
            
            PHRASE:
            {searchPhrase}
            """;

    public BookController(ChatClient.Builder builder, RAGTools ragTools) {
        this.client = builder.build();
        this.ragTools = ragTools;
    }

    //Retrieval Augmented Generation with Neo4j - vector search + retrieval query for related context
    @GetMapping("/graphTool")
    public String generateResponseWithContext2(@RequestParam String searchPhrase) {
        PromptTemplate prompt2 = new PromptTemplate("""
            You are a book expert providing recommendations from high-quality book information provided.
            Please summarize the books provided.
            
            PHRASE:
            {searchPhrase}
            """);

        return client.prompt(prompt2.create(Map.of("searchPhrase", searchPhrase)))
                .advisors(new SimpleLoggerAdvisor())
                .tools(ragTools)
                .call().content();
    }
}
