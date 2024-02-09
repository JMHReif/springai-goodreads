package com.jmhreif.springaigoodreads;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.openai.OpenAiEmbeddingClient;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.Neo4jVectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringaiGoodreadsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringaiGoodreadsApplication.class, args);
	}

	@Bean
	public Driver driver() {
		return GraphDatabase.driver(System.getenv("SPRING_NEO4J_URI"),
				AuthTokens.basic(System.getenv("SPRING_NEO4J_AUTHENTICATION_USERNAME"),
						System.getenv("SPRING_NEO4J_AUTHENTICATION_PASSWORD")));
	}

	@Bean
	public EmbeddingClient embeddingClient() {
		return new OpenAiEmbeddingClient(new OpenAiApi(System.getenv("SPRING_AI_OPENAI_API_KEY")));
	}

	@Bean
	public Neo4jVectorStore vectorStore(Driver driver, EmbeddingClient embeddingClient) {

		return new Neo4jVectorStore(driver, embeddingClient,
				Neo4jVectorStore.Neo4jVectorStoreConfig.builder().withLabel("Review").build());
	}
}
