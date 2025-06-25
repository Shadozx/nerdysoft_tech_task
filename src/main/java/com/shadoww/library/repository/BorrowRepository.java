package com.shadoww.library.repository;

import com.shadoww.library.model.Book;
import com.shadoww.library.model.Borrow;
import com.shadoww.library.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    List<Borrow> findByMemberAndReturnedFalse(Member member);

    List<Borrow> findByBookAndReturnedFalse(Book book);

    List<Borrow> findByReturnedFalse();

    List<Borrow> findByMember_NameIgnoreCase(String name);

}