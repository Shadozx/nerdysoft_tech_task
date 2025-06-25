package com.shadoww.library.service.impl;


import com.shadoww.library.model.Book;
import com.shadoww.library.model.Borrow;
import com.shadoww.library.repository.BookRepository;
import com.shadoww.library.repository.BorrowRepository;
import com.shadoww.library.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class BookServiceImplTests {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowRepository borrowRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrIncrement_shouldCreateNewBook_ifNotExists() {

        Book book = new Book();
        book.setTitle("Clean Code");
        book.setAuthor("Robert Martin");

        when(bookRepository.findByTitleIgnoreCaseAndAuthorIgnoreCase("Clean Code", "Robert Martin"))
                .thenReturn(Optional.empty());

        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.createOrIncrement(book);

        assertThat(result.getTitle()).isEqualTo("Clean Code");
        assertThat(result.getAuthor()).isEqualTo("Robert Martin");
        assertThat(result.getAmount()).isEqualTo(1);
        verify(bookRepository).save(book);
    }

    @Test
    void createOrIncrement_shouldIncrementAmount_ifBookExists() {

        Book existing = new Book();
        existing.setId(1L);
        existing.setTitle("Clean Code");
        existing.setAuthor("Robert Martin");
        existing.setAmount(2);

        when(bookRepository.findByTitleIgnoreCaseAndAuthorIgnoreCase("Clean Code", "Robert Martin"))
                .thenReturn(Optional.of(existing));

        when(bookRepository.save(existing)).thenReturn(existing);

        Book input = new Book();
        input.setTitle("Clean Code");
        input.setAuthor("Robert Martin");

        Book result = bookService.createOrIncrement(input);

        assertThat(result.getAmount()).isEqualTo(3);
        verify(bookRepository).save(existing);
    }

    @Test
    void update_shouldUpdateExistingBook() {
        long bookId = 10L;

        Book existing = new Book();
        existing.setId(bookId);
        existing.setTitle("Old Title");
        existing.setAuthor("Old Author");
        existing.setAmount(5);

        Book updated = new Book();
        updated.setTitle("New Title");
        updated.setAuthor("New Author");
        updated.setAmount(42);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existing));
        when(bookRepository.save(existing)).thenReturn(existing);

        Book result = bookService.update(bookId, updated);

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getAuthor()).isEqualTo("New Author");
        assertThat(result.getAmount()).isEqualTo(42);
        verify(bookRepository).save(existing);
    }

    @Test
    void update_shouldThrow_whenBookNotFound() {
        long bookId = 123L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        Book updated = new Book();
        updated.setTitle("X");
        updated.setAuthor("Y");
        updated.setAmount(10);

        assertThatThrownBy(() -> bookService.update(bookId, updated))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Book not found");
    }

    @Test
    void delete_shouldDeleteBook_ifNotBorrowed() {
        long bookId = 99L;

        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Book");
        book.setAuthor("Author");
        book.setAmount(1);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowRepository.findByBookAndReturnedFalse(book)).thenReturn(Collections.emptyList());

        bookService.delete(bookId);

        verify(bookRepository).delete(book);
    }

    @Test
    void delete_shouldThrow_ifBookIsBorrowed() {
        long bookId = 77L;
        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Book");
        book.setAuthor("Author");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowRepository.findByBookAndReturnedFalse(book)).thenReturn(List.of(mock(Borrow.class)));

        assertThatThrownBy(() -> bookService.delete(bookId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("currently borrowed");
    }

    @Test
    void findById_shouldReturnBook_ifExists() {
        long bookId = 1L;
        String title = "Java";

        Book book = new Book();
        book.setId(bookId);
        book.setTitle(title);
        book.setAuthor("Someone");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        Optional<Book> result = bookService.findById(bookId);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo(title);
    }

    @Test
    void findAll_shouldReturnListOfBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(new Book(), new Book()));

        List<Book> result = bookService.findAll();

        assertThat(result).hasSize(2);
    }
}
