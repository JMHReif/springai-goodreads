package com.jmhreif.springaigoodreads;

import org.springframework.data.neo4j.core.schema.Id;

public record Review(@Id String id,
                     String text,
                     Integer rating) {
}
