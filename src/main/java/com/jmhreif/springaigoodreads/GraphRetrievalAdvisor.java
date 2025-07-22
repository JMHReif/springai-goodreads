package com.jmhreif.springaigoodreads;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GraphRetrievalAdvisor implements CallAdvisor {
    private final BookRepository repo;

    public GraphRetrievalAdvisor(BookRepository repo) {
        this.repo = repo;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        var similarDocIds = (List<String>) chatClientRequest.context().get("similarDocIds");
        // System.out.println("similarDocIds: " + similarDocIds);

        // Run graph retrieval query
        List<Book> bookList = repo.findBooks(similarDocIds);
        // System.out.println("--- Book list ---");
        // System.out.println(bookList);

        // Create system message with book context
        String bookContext = bookList.stream()
                .map(Book::toString)
                .collect(Collectors.joining("\n"));
        SystemMessage systemMessage = new SystemMessage("Use these books for context: " + bookContext);
        
        // Create new prompt with system message and original user message
        var updatedPrompt = new Prompt(List.of(chatClientRequest.prompt().getUserMessage(), systemMessage));
        
        var updatedRequest = ChatClientRequest.builder()
                .prompt(updatedPrompt)
                .context(chatClientRequest.context())
                .build();
        System.out.println("----- Updated request -----");
        System.out.println(updatedRequest);

        return callAdvisorChain.nextCall(updatedRequest);
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
