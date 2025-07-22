package com.jmhreif.springaigoodreads;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.stream.Collectors;

public class GraphRetrievalAdvisor implements CallAdvisor {
    private final BookRepository repo;

    public GraphRetrievalAdvisor(BookRepository repo) {
        this.repo = repo;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        // Get documents from QuestionAnswerAdvisor context
        var documents = (List<Document>) chatClientRequest.context().get("qa_retrieved_documents");
        
        if (documents == null || documents.isEmpty()) {
            System.out.println("No documents found in context, skipping graph retrieval");
            return callAdvisorChain.nextCall(chatClientRequest);
        }

        // Extract document IDs from QuestionAnswerAdvisor
        List<String> similarDocIds = documents.stream().map(Document::getId).collect(Collectors.toList());

        // Run graph retrieval query
        List<Book> bookList = repo.findBooks(similarDocIds);

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
