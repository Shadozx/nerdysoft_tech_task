package com.shadoww.library.service;

import com.shadoww.library.model.Borrow;

import java.util.List;

public interface BorrowService {


    Borrow borrowBook(Long memberId, Long bookId);

    Borrow returnBook(Long borrowId);

    List<Borrow> getBorrowsByMemberName(String name);

    List<String> getAllDistinctBorrowedBookTitles();

    List<Object[]> getAllBorrowedBookTitlesWithCount(); // title, count
}
