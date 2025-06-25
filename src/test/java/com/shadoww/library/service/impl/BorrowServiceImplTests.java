package com.shadoww.library.service.impl;


import com.shadoww.library.dto.BorrowCountDto;
import com.shadoww.library.model.Book;
import com.shadoww.library.model.Borrow;
import com.shadoww.library.model.Member;
import com.shadoww.library.repository.BorrowRepository;
import com.shadoww.library.service.BookService;
import com.shadoww.library.service.MemberService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class BorrowServiceImplTests {


    @Mock
    private BorrowRepository borrowRepository;
    @Mock
    private MemberService memberService;
    @Mock
    private BookService bookService;

    private BorrowServiceImpl borrowService;

    private final Long memberId = 1L;
    private final Long bookId = 2L;
    private final Long borrowId = 3L;
    private final String memberName = "John";
    private final String titleX = "Book X";
    private final String titleY = "Book Y";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        borrowService = new BorrowServiceImpl(borrowRepository, memberService, bookService);
        ReflectionTestUtils.setField(borrowService, "borrowLimit", 10);
    }

    // === borrowBook ===

    @Test
    void borrowBook_shouldSucceed_whenValid() {
        Member member = new Member();
        member.setId(memberId);

        Book book = new Book();
        book.setId(bookId);
        book.setAmount(2);

        when(memberService.findById(memberId)).thenReturn(member);
        when(bookService.findById(bookId)).thenReturn(book);
        when(borrowRepository.findByMemberAndReturnedFalse(member)).thenReturn(Collections.emptyList());
        when(borrowRepository.save(any())).thenAnswer(inv -> {
            Borrow b = inv.getArgument(0);
            b.setBorrowDate(LocalDateTime.now());
            return b;
        });

        Borrow result = borrowService.borrowBook(memberId, bookId);

        assertThat(result.getMember()).isEqualTo(member);
        assertThat(result.getBook()).isEqualTo(book);
        assertThat(book.getAmount()).isEqualTo(1); // зменшено
    }

    @Test
    void borrowBook_shouldThrow_whenBookNotAvailable() {
        Member member = new Member();
        Book book = new Book();
        book.setAmount(0);

        when(memberService.findById(memberId)).thenReturn(member);
        when(bookService.findById(bookId)).thenReturn(book);
        when(borrowRepository.findByMemberAndReturnedFalse(member)).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> borrowService.borrowBook(memberId, bookId));
    }

    @Test
    void borrowBook_shouldThrow_whenLimitExceeded() {
        Member member = new Member();
        Book book = new Book();
        book.setAmount(5);

        List<Borrow> borrows = new ArrayList<>();
        for (int i = 0; i < 10; i++) borrows.add(new Borrow());

        when(memberService.findById(memberId)).thenReturn(member);
        when(bookService.findById(bookId)).thenReturn(book);
        when(borrowRepository.findByMemberAndReturnedFalse(member)).thenReturn(borrows);

        assertThrows(IllegalStateException.class, () -> borrowService.borrowBook(memberId, bookId));
    }

    // === returnBook ===

    @Test
    void returnBook_shouldSucceed_whenValid() {
        Book book = new Book();
        book.setAmount(1);

        Borrow borrow = new Borrow();
        borrow.setBook(book);
        borrow.setReturned(false);

        when(borrowRepository.findById(borrowId)).thenReturn(Optional.of(borrow));
        when(borrowRepository.save(borrow)).thenReturn(borrow);

        Borrow result = borrowService.returnBook(borrowId);

        assertThat(result.isReturned()).isTrue();
        assertThat(result.getReturnDate()).isNotNull();
        assertThat(book.getAmount()).isEqualTo(2);
    }

    @Test
    void returnBook_shouldThrow_whenAlreadyReturned() {
        Borrow borrow = new Borrow();
        borrow.setReturned(true);

        when(borrowRepository.findById(borrowId)).thenReturn(Optional.of(borrow));

        assertThrows(IllegalStateException.class, () -> borrowService.returnBook(borrowId));
    }

    @Test
    void returnBook_shouldThrow_whenBorrowNotFound() {
        when(borrowRepository.findById(borrowId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> borrowService.returnBook(borrowId));
    }

    // === getBorrowsByMemberName ===

    @Test
    void getBorrowsByMemberName_shouldReturnBorrows() {
        List<Borrow> borrows = List.of(new Borrow(), new Borrow());

        when(borrowRepository.findByMember_NameIgnoreCase(memberName)).thenReturn(borrows);

        List<Borrow> result = borrowService.getBorrowsByMemberName(memberName);

        assertThat(result).isEqualTo(borrows);
    }

    // === getAllDistinctBorrowedBookTitles ===

    @Test
    void getAllDistinctBorrowedBookTitles_shouldReturnUniqueTitles() {
        Book bookX = new Book(); bookX.setTitle(titleX);
        Book bookY = new Book(); bookY.setTitle(titleY);

        Borrow b1 = new Borrow(); b1.setBook(bookX);
        Borrow b2 = new Borrow(); b2.setBook(bookY);
        Borrow b3 = new Borrow(); b3.setBook(bookX);

        when(borrowRepository.findByReturnedFalse()).thenReturn(List.of(b1, b2, b3));

        List<String> result = borrowService.getAllDistinctBorrowedBookTitles();

        assertThat(result).containsExactlyInAnyOrder(titleX, titleY);
    }

    // === getAllBorrowedBookTitlesWithCount ===

    @Test
    void getAllBorrowedBookTitlesWithCount_shouldReturnGroupedCounts() {
        Book bookX = new Book(); bookX.setTitle(titleX);
        Book bookY = new Book(); bookY.setTitle(titleY);

        Borrow b1 = new Borrow(); b1.setBook(bookX);
        Borrow b2 = new Borrow(); b2.setBook(bookX);
        Borrow b3 = new Borrow(); b3.setBook(bookY);

        when(borrowRepository.findByReturnedFalse()).thenReturn(List.of(b1, b2, b3));

        List<BorrowCountDto> result = borrowService.getAllBorrowedBookTitlesWithCount();

        Map<String, Long> map = result.stream()
                .collect(Collectors.toMap(BorrowCountDto::title, BorrowCountDto::count));

        assertThat(map.get(titleX)).isEqualTo(2L);
        assertThat(map.get(titleY)).isEqualTo(1L);
    }
}
