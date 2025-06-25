package com.shadoww.library.controller;

import com.shadoww.library.dto.BookRequestDto;
import com.shadoww.library.dto.BookResponseDto;
import com.shadoww.library.model.Book;
import com.shadoww.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;


    @PostMapping
    @Operation(summary = "Create a new book or increment amount if exists")
    public ResponseEntity<BookResponseDto> create(
            @RequestBody @Valid BookRequestDto requestDto
    ) {
        Book created = bookService.createOrIncrement(toEntity(requestDto));
        return new ResponseEntity<>(toDto(created), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all books")
    public List<BookResponseDto> getAll() {
        return bookService.findAll().stream()
                .map(this::toDto)
                .collect(toList());
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get a book by ID")
    public ResponseEntity<BookResponseDto> getById(
            @PathVariable Long id
    ) {
        return bookService.findById(id)
                .map(book -> ResponseEntity.ok(toDto(book)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing book")
    public ResponseEntity<BookResponseDto> update(
            @PathVariable Long id,
            @RequestBody @Valid BookRequestDto requestDto
    ) {
        Book updated = bookService.update(id, toEntity(requestDto));
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book if not borrowed")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // methods for converting from DTO to Entity and vice versa

    private Book toEntity(BookRequestDto dto) {
        Book book = new Book();

        book.setTitle(dto.title());
        book.setAuthor(dto.author());

        return book;
    }

    private BookResponseDto toDto(Book book) {
        return new BookResponseDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getAmount()
        );
    }
}
