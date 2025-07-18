package com.jmhreif.springaigoodreads;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.aop.framework.Advised;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomVectorSearchAdvisor implements CallAdvisor {
    private final Neo4jVectorStore vectorStore;

    public CustomVectorSearchAdvisor(Neo4jVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        List<Document> results = vectorStore.similaritySearch(chatClientRequest.prompt().getUserMessage().getText());
//        System.out.println("----- SIMILAR DOCS -----");
//        System.out.println(results);

        HashMap<String, Object> updatedContext = new HashMap<>(chatClientRequest.context());
        updatedContext.put("similarDocIds", results.stream().map(Document::getId).collect(Collectors.toList()));

        ChatClientResponse response = ChatClientResponse.builder().context(updatedContext).build();
        System.out.println("----- Similarity Search Response -----");
        System.out.println(response);

        return response;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
