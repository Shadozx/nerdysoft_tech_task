package com.shadoww.library.service.impl;

import com.shadoww.library.model.Book;
import com.shadoww.library.repository.BookRepository;
import com.shadoww.library.repository.BorrowRepository;
import com.shadoww.library.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;


    @Override
    @Transactional
    public Book createOrIncrement(Book book) {
        validate(book);

        return bookRepository.findByTitleIgnoreCaseAndAuthorIgnoreCase(book.getTitle(), book.getAuthor())
                .map(existing -> {
                    existing.setAmount(existing.getAmount() + 1);
                    return bookRepository.save(existing);
                })
                .orElseGet(() -> bookRepository.save(book));
    }

    @Override
    @Transactional
    public Book update(Long id, Book updatedBook) {
        validate(updatedBook);

        Book existing = findById(id);

        existing.setTitle(updatedBook.getTitle());
        existing.setAuthor(updatedBook.getAuthor());
        existing.setAmount(updatedBook.getAmount());

        return bookRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Book book = findById(id);

        boolean isBorrowed = !borrowRepository.findByBookAndReturnedFalse(book).isEmpty();

        if (isBorrowed) {
            throw new IllegalStateException("Cannot delete book that is currently borrowed");
        }

        bookRepository.delete(book);
    }

    @Override
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));
    }

    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    private void validate(Book book) {
        if(Objects.isNull(book)) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        if (Objects.isNull(book.getTitle()) || book.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Book must have title");
        }

        if (Objects.isNull(book.getAuthor()) || book.getAuthor().isEmpty()) {
            throw new IllegalArgumentException("Book must have author");
        }

        if (book.getAmount() < 0) {
            throw new IllegalArgumentException("Book mustn't have negative amount");
        }
    }
}
