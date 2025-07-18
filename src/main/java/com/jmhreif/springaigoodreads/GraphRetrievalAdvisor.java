package com.jmhreif.springaigoodreads;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

public class GraphRetrievalAdvisor implements CallAdvisor {
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        System.out.println("REQUEST to retrieval: " + chatClientRequest);

        var similarDocs = chatClientRequest.context().get("updatedContext");
        System.out.println("similarDocs: " + similarDocs);

        return ChatClientResponse.builder().context("similarDocs", similarDocs).build();
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
