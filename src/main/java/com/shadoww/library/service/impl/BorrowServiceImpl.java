package com.shadoww.library.service.impl;

import com.shadoww.library.dto.BorrowCountDto;
import com.shadoww.library.model.Book;
import com.shadoww.library.model.Borrow;
import com.shadoww.library.model.Member;
import com.shadoww.library.repository.BorrowRepository;
import com.shadoww.library.service.BookService;
import com.shadoww.library.service.BorrowService;
import com.shadoww.library.service.MemberService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRepository borrowRepository;
    private final MemberService memberService;
    private final BookService bookService;

    @Value("${borrow.limit}")
    private int borrowLimit;

    @Override
    @Transactional
    public Borrow borrowBook(Long memberId, Long bookId) {
        Member member = memberService.findById(memberId);

        Book book = bookService.findById(bookId);

        int activeBorrows = borrowRepository.findByMemberAndReturnedFalse(member).size();

        if (activeBorrows >= borrowLimit) {
            throw new IllegalStateException("Borrow limit exceeded");
        }


        if (book.getAmount() <= 0) {
            throw new IllegalStateException("Book is not available");
        }

        book.setAmount(book.getAmount() - 1);
        Borrow borrow = new Borrow();
        borrow.setBook(book);
        borrow.setMember(member);

        return borrowRepository.save(borrow);
    }

    @Override
    @Transactional
    public Borrow returnBook(Long borrowId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new EntityNotFoundException("Borrow not found with id: " + borrowId));

        if (borrow.isReturned()) {
            throw new IllegalStateException("Already returned");
        }

        borrow.setReturned(true);
        borrow.setReturnDate(LocalDateTime.now());

        Book book = borrow.getBook();
        book.setAmount(book.getAmount() + 1);

        return borrowRepository.save(borrow);
    }

    @Override
    public List<Borrow> getBorrowsByMemberName(String name) {
        return borrowRepository.findByMember_NameIgnoreCase(name);
    }

    @Override
    public List<String> getAllDistinctBorrowedBookTitles() {
        return borrowRepository.findByReturnedFalse().stream()
                .map(b -> b.getBook().getTitle())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowCountDto> getAllBorrowedBookTitlesWithCount() {
        return borrowRepository.findByReturnedFalse().stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBook().getTitle(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(e -> new BorrowCountDto(e.getKey(), e.getValue()))
                .toList();

    }
}
