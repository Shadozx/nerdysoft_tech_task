package com.shadoww.library.service.impl;


import com.shadoww.library.model.Book;
import com.shadoww.library.model.Borrow;
import com.shadoww.library.repository.BookRepository;
import com.shadoww.library.repository.BorrowRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BookServiceImplTests {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowRepository borrowRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private final Long bookId = 1L;
    private final String title = "Clean Code";
    private final String author = "Robert Martin";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrIncrement_shouldCreateNewBook_whenNotExists() {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setAmount(1);

        when(bookRepository.findByTitleIgnoreCaseAndAuthorIgnoreCase(title, author)).thenReturn(Optional.empty());
        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.createOrIncrement(book);

        assertThat(result).isEqualTo(book);
    }

    @Test
    void createOrIncrement_shouldIncrement_whenBookExists() {
        Book existing = new Book();
        existing.setId(bookId);
        existing.setTitle(title);
        existing.setAuthor(author);
        existing.setAmount(2);

        Book input = new Book();
        input.setTitle(title);
        input.setAuthor(author);

        when(bookRepository.findByTitleIgnoreCaseAndAuthorIgnoreCase(title, author)).thenReturn(Optional.of(existing));
        when(bookRepository.save(existing)).thenReturn(existing);

        Book result = bookService.createOrIncrement(input);

        assertThat(result.getAmount()).isEqualTo(3);
    }

    @Test
    void createOrIncrement_shouldThrow_whenBookIsNull() {
        assertThrows(IllegalArgumentException.class, () -> bookService.createOrIncrement(null));
    }

    @Test
    void createOrIncrement_shouldThrow_whenTitleIsMissing() {
        Book book = new Book();
        book.setAuthor(author);
        book.setAmount(1);

        assertThrows(IllegalArgumentException.class, () -> bookService.createOrIncrement(book));
    }

    @Test
    void createOrIncrement_shouldThrow_whenAuthorIsMissing() {
        Book book = new Book();
        book.setTitle(title);
        book.setAmount(1);

        assertThrows(IllegalArgumentException.class, () -> bookService.createOrIncrement(book));
    }

    @Test
    void createOrIncrement_shouldThrow_whenAmountNegative() {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setAmount(-1);

        assertThrows(IllegalArgumentException.class, () -> bookService.createOrIncrement(book));
    }

    // -------- update --------

    @Test
    void update_shouldUpdate_whenValid() {
        Book existing = new Book();
        existing.setId(bookId);
        existing.setTitle("Old");
        existing.setAuthor("Old");
        existing.setAmount(1);

        Book updated = new Book();
        updated.setTitle(title);
        updated.setAuthor(author);
        updated.setAmount(5);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existing));
        when(bookRepository.save(existing)).thenReturn(existing);

        Book result = bookService.update(bookId, updated);

        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getAuthor()).isEqualTo(author);
        assertThat(result.getAmount()).isEqualTo(5);
    }

    @Test
    void update_shouldThrow_whenBookNotFound() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        Book updated = new Book();
        updated.setTitle(title);
        updated.setAuthor(author);
        updated.setAmount(1);

        assertThrows(EntityNotFoundException.class, () -> bookService.update(bookId, updated));
    }

    @Test
    void update_shouldThrow_whenInvalidBook() {
        Book updated = new Book(); // no title, author, amount

        assertThrows(IllegalArgumentException.class, () -> bookService.update(bookId, updated));
    }

    // -------- delete --------

    @Test
    void delete_shouldDelete_whenNotBorrowed() {
        Book book = new Book();
        book.setId(bookId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowRepository.findByBookAndReturnedFalse(book)).thenReturn(Collections.emptyList());

        bookService.delete(bookId);

        verify(bookRepository).delete(book);
    }

    @Test
    void delete_shouldThrow_whenBorrowed() {
        Book book = new Book();
        book.setId(bookId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowRepository.findByBookAndReturnedFalse(book)).thenReturn(List.of(new Borrow()));

        assertThrows(IllegalStateException.class, () -> bookService.delete(bookId));
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.delete(bookId));
    }

    // -------- findById & findAll --------

    @Test
    void findById_shouldReturn_whenExists() {
        Book book = new Book();
        book.setId(bookId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        Book result = bookService.findById(bookId);

        assertThat(result).isEqualTo(book);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.findById(bookId));
    }

    @Test
    void findAll_shouldReturnAllBooks() {
        List<Book> books = List.of(new Book(), new Book());

        when(bookRepository.findAll()).thenReturn(books);

        List<Book> result = bookService.findAll();

        assertThat(result).isEqualTo(books);
    }
}
