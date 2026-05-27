package com.jmhreif.springaigoodreads;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

public interface UserRepository extends Neo4jRepository<User, String> {
    @Query("MATCH (u:User)-[:PUBLISHED]->(r:Review)<-[rel:WRITTEN_FOR]-(b:Book) " +
            "WHERE u.user_id = $userId " +
            "RETURN b, collect(rel), collect(r)")
    List<Book> findBooksByUser(String userId);

    @Query("MATCH (u:User {user_id: $userId})-[:PUBLISHED]->(:Review)-[:WRITTEN_FOR]->(readBook:Book) " +
            "MATCH (readBook)<-[:WRITTEN_FOR]-(:Review)<-[:PUBLISHED]-(similar:User) " +
            "WHERE similar.user_id <> $userId " +
            "MATCH (similar)-[:PUBLISHED]->(r:Review)-[rel:WRITTEN_FOR]->(rec:Book) " +
            "WHERE NOT (u)-[:PUBLISHED]->(:Review)-[:WRITTEN_FOR]->(rec) " +
            "  AND r.rating >= 4 " +
            "RETURN rec, collect(rel), collect(r) " +
            "ORDER BY count(DISTINCT similar) DESC " +
            "LIMIT 10")
    List<Book> findCollaborativeRecommendations(String userId);
}
