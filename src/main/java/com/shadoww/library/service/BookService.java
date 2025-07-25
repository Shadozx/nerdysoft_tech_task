package com.shadoww.library.service;

import com.shadoww.library.model.Book;

import java.util.List;

public interface BookService {

    Book createOrIncrement(Book book);

    Book update(Long id, Book updatedBook);

    void delete(Long id);

    Book findById(Long id);

    List<Book> findAll();
}
