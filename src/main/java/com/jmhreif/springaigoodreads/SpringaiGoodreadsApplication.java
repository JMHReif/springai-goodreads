package com.jmhreif.springaigoodreads;

import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringaiGoodreadsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringaiGoodreadsApplication.class, args);
	}

	@Bean
	public RAGTools ragTools(Neo4jVectorStore vectorStore, BookRepository bookRepository) {
		return new RAGTools(vectorStore, bookRepository);
	}
}
