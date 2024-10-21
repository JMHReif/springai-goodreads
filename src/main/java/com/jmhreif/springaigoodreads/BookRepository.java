package com.jmhreif.springaigoodreads;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

public interface BookRepository extends Neo4jRepository<Book, String> {
    @Query("MATCH (b:Book)<-[rel:WRITTEN_FOR]-(r:Review) " +
            "WHERE r.id IN $reviewIds " +
            "RETURN b, collect(rel), collect(r);")
    List<Book> findBooks(List<String> reviewIds);
}
