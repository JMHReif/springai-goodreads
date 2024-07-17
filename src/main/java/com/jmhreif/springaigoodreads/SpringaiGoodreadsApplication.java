package com.jmhreif.springaigoodreads;

import org.neo4j.driver.Driver;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
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
	public EmbeddingModel embeddingModel() {
		return new OpenAiEmbeddingModel(new OpenAiApi(System.getenv("SPRING_AI_OPENAI_API_KEY")));
	}

	@Bean
	public Neo4jVectorStore vectorStore(Driver driver, EmbeddingModel embeddingModel) {

		return new Neo4jVectorStore(driver, embeddingModel,
				Neo4jVectorStore.Neo4jVectorStoreConfig.builder()
						.withLabel("Review")
						.withIndexName("review-embedding-index")
						.build(), false);
	}
}
