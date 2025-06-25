package com.shadoww.library.service.impl;

import com.shadoww.library.model.Book;
import com.shadoww.library.model.Borrow;
import com.shadoww.library.model.Member;
import com.shadoww.library.repository.BookRepository;
import com.shadoww.library.repository.BorrowRepository;
import com.shadoww.library.repository.MemberRepository;
import com.shadoww.library.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRepository borrowRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    @Value("${borrow.limit}")
    private int borrowLimit;

    @Override
    @Transactional
    public Borrow borrowBook(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        int activeBorrows = borrowRepository.findByMemberAndReturnedFalse(member).size();

        if (activeBorrows >= borrowLimit) {
            throw new IllegalStateException("Borrow limit exceeded");
        }


        if (book.getAmount() == 0) {
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
                .orElseThrow(() -> new RuntimeException("Borrow not found"));

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
    @Transactional
    public List<Borrow> getBorrowsByMemberName(String name) {
        return borrowRepository.findByMember_NameIgnoreCase(name);
    }

    @Override
    @Transactional
    public List<String> getAllDistinctBorrowedBookTitles() {
        return borrowRepository.findByReturnedFalse().stream()
                .map(b -> b.getBook().getTitle())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Object[]> getAllBorrowedBookTitlesWithCount() {
        Map<String, Long> grouped = borrowRepository.findByReturnedFalse().stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBook().getTitle(),
                        Collectors.counting()
                ));

        return grouped.entrySet().stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toList();
    }
}
