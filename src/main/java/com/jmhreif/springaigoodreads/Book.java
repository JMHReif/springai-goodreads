package com.jmhreif.springaigoodreads;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

public record Book(@Id String book_id,
                   String title,
                   String isbn,
                   String isbn13,
                   @Relationship(value = "WRITTEN_FOR", direction = Relationship.Direction.INCOMING) List<Review> reviewList) {
}
