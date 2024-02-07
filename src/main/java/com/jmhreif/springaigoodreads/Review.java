package com.jmhreif.springaigoodreads;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Relationship;

public record Review(@Id String id,
                     String text,
                     Integer rating,
                     @Relationship(type = "WRITTEN_FOR") Book reviewedBook) {
}
