package com.shadoww.library.repository;

import com.shadoww.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByTitleIgnoreCaseAndAuthorIgnoreCase(String title, String author);

}
