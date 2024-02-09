package com.jmhreif.springaigoodreads;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

public interface ReviewRepository extends Neo4jRepository<Review, String> {
    @Query("MATCH (r:Review)-[rel:WRITTEN_FOR]->(b:Book) " +
            "WHERE r.review_id IN $reviewIds " +
            "RETURN r, collect(rel), collect(b)")
    Iterable<Review> findBooks(List<String> reviewIds);
}
