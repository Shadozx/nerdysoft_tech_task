package com.shadoww.library.service;

import com.shadoww.library.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookService {

    Book createOrIncrement(Book book);

    Book update(Long id, Book updatedBook);

    void delete(Long id);

    Optional<Book> findById(Long id);

    List<Book> findAll();
}
