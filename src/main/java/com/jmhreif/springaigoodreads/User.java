package com.jmhreif.springaigoodreads;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

public record User(@Id String user_id,
                   @Relationship(value = "PUBLISHED", direction = Relationship.Direction.OUTGOING) List<Review> reviews) {
}
