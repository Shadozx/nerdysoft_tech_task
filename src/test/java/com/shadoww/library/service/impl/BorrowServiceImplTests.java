package com.shadoww.library.service.impl;


import com.shadoww.library.model.Book;
import com.shadoww.library.model.Borrow;
import com.shadoww.library.model.Member;
import com.shadoww.library.repository.BookRepository;
import com.shadoww.library.repository.BorrowRepository;
import com.shadoww.library.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BorrowServiceImplTests {


    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BorrowRepository borrowRepository;

    @InjectMocks
    private BorrowServiceImpl borrowService;

    private final Long memberId = 1L;
    private final Long bookId = 5L;
    private final Long secondBookId = 2L;
    private final Long borrowId = 10L;
    private final String memberName = "John";
    private final String bookTitle = "Book";
    private final String bookAuthor = "Author";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        borrowService = new BorrowServiceImpl(borrowRepository, memberRepository, bookRepository);
        ReflectionTestUtils.setField(borrowService, "borrowLimit", 10);
    }

    @Test
    void borrowBook_shouldSucceed_whenValid() {
        Member member = new Member();
        member.setId(memberId);
        member.setName(memberName);

        Book book = new Book();
        book.setId(bookId);
        book.setTitle(bookTitle);
        book.setAuthor(bookAuthor);
        book.setAmount(2);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowRepository.findByMemberAndReturnedFalse(member)).thenReturn(Collections.emptyList());
        when(borrowRepository.save(any(Borrow.class))).thenAnswer(inv -> {
            Borrow b = inv.getArgument(0);
            b.setBorrowDate(LocalDateTime.now());
            return b;
        });

        Borrow result = borrowService.borrowBook(memberId, bookId);

        assertThat(result.getBook()).isEqualTo(book);
        assertThat(result.getMember()).isEqualTo(member);
        assertThat(result.isReturned()).isFalse();
        assertThat(result.getBorrowDate()).isNotNull();
        assertThat(book.getAmount()).isEqualTo(1);
    }

    @Test
    void borrowBook_shouldFail_whenBookAmountIsZero() {
        Member member = new Member();
        member.setId(memberId);

        Book book = new Book();
        book.setId(secondBookId);
        book.setAmount(0);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(bookRepository.findById(secondBookId)).thenReturn(Optional.of(book));
        when(borrowRepository.findByMemberAndReturnedFalse(member)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> borrowService.borrowBook(memberId, secondBookId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void borrowBook_shouldFail_whenLimitExceeded() {
        Member member = new Member();
        member.setId(memberId);

        Book book = new Book();
        book.setId(secondBookId);
        book.setAmount(5);

        List<Borrow> borrows = new ArrayList<>();
        for (int i = 0; i < 10; i++) borrows.add(new Borrow());

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(bookRepository.findById(secondBookId)).thenReturn(Optional.of(book));
        when(borrowRepository.findByMemberAndReturnedFalse(member)).thenReturn(borrows);

        assertThatThrownBy(() -> borrowService.borrowBook(memberId, secondBookId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Borrow limit exceeded");
    }

    @Test
    void borrowBook_shouldFail_whenMemberNotFound() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.borrowBook(memberId, bookId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Member not found");
    }

    @Test
    void borrowBook_shouldFail_whenBookNotFound() {
        Member member = new Member();
        member.setId(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.borrowBook(memberId, bookId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Book not found");
    }

    @Test
    void returnBook_shouldWork_whenValid() {
        Book book = new Book();
        book.setAmount(1);

        Borrow borrow = new Borrow();
        borrow.setId(borrowId);
        borrow.setReturned(false);
        borrow.setBook(book);

        when(borrowRepository.findById(borrowId)).thenReturn(Optional.of(borrow));
        when(borrowRepository.save(borrow)).thenReturn(borrow);

        Borrow result = borrowService.returnBook(borrowId);

        assertThat(result.isReturned()).isTrue();
        assertThat(result.getReturnDate()).isNotNull();
        assertThat(book.getAmount()).isEqualTo(2);
    }

    @Test
    void returnBook_shouldFail_whenAlreadyReturned() {
        Borrow borrow = new Borrow();
        borrow.setReturned(true);

        when(borrowRepository.findById(borrowId)).thenReturn(Optional.of(borrow));

        assertThatThrownBy(() -> borrowService.returnBook(borrowId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Already returned");
    }

    @Test
    void returnBook_shouldFail_whenNotFound() {
        when(borrowRepository.findById(borrowId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.returnBook(borrowId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Borrow not found");
    }

    @Test
    void getBorrowsByMemberName_shouldReturnList() {
        when(borrowRepository.findByMember_NameIgnoreCase(memberName))
                .thenReturn(List.of(new Borrow(), new Borrow()));

        List<Borrow> result = borrowService.getBorrowsByMemberName(memberName);

        assertThat(result).hasSize(2);
    }

    @Test
    void getAllDistinctBorrowedBookTitles_shouldReturnUniqueTitles() {
        Book book1 = new Book();
        book1.setTitle("A");

        Book book2 = new Book();
        book2.setTitle("B");

        Borrow b1 = new Borrow();
        b1.setBook(book1);

        Borrow b2 = new Borrow();
        b2.setBook(book2);

        Borrow b3 = new Borrow();
        b3.setBook(book1);

        when(borrowRepository.findByReturnedFalse()).thenReturn(List.of(b1, b2, b3));

        List<String> result = borrowService.getAllDistinctBorrowedBookTitles();

        assertThat(result).containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void getAllBorrowedBookTitlesWithCount_shouldGroupCorrectly() {
        Book bookX1 = new Book();
        bookX1.setTitle("X");

        Book bookX2 = new Book();
        bookX2.setTitle("X");

        Book bookY = new Book();
        bookY.setTitle("Y");

        Borrow br1 = new Borrow();
        br1.setBook(bookX1);

        Borrow br2 = new Borrow();
        br2.setBook(bookX2);

        Borrow br3 = new Borrow();
        br3.setBook(bookY);

        when(borrowRepository.findByReturnedFalse()).thenReturn(List.of(br1, br2, br3));

        List<Object[]> result = borrowService.getAllBorrowedBookTitlesWithCount();

        assertThat(result).hasSize(2);

        Map<String, Long> map = result.stream()
                .collect(Collectors.toMap(obj -> (String) obj[0], obj -> (Long) obj[1]));

        assertThat(map.get("X")).isEqualTo(2L);
        assertThat(map.get("Y")).isEqualTo(1L);
    }
}
