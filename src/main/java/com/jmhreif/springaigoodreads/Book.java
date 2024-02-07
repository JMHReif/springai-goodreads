package com.jmhreif.springaigoodreads;

import org.springframework.data.neo4j.core.schema.Id;

public record Book(@Id String book_id,
                   String title,
                   String isbn,
                   String isbn13) {
}
