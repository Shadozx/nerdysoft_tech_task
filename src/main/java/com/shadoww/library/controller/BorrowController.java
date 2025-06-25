package com.shadoww.library.controller;

import com.shadoww.library.dto.BorrowRequestDto;
import com.shadoww.library.dto.BorrowResponseDto;
import com.shadoww.library.dto.BorrowedBookDto;
import com.shadoww.library.model.Borrow;
import com.shadoww.library.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;


    @PostMapping
    @Operation(summary = "Borrow a book")
    public ResponseEntity<BorrowResponseDto> borrowBook(@RequestBody @Valid BorrowRequestDto dto) {
        Borrow borrow = borrowService.borrowBook(dto.memberId(), dto.bookId());
        return new ResponseEntity<>(toDto(borrow), HttpStatus.CREATED);
    }

    @PostMapping("/{borrowId}/return")
    @Operation(summary = "Return a borrowed book")
    public ResponseEntity<BorrowResponseDto> returnBook(@PathVariable Long borrowId) {
        Borrow borrow = borrowService.returnBook(borrowId);

        return ResponseEntity.ok(toDto(borrow));
    }

    @GetMapping("/by-member")
    @Operation(summary = "Get all books borrowed by a specific member (by name)")
    public ResponseEntity<List<BorrowedBookDto>> getByMemberName(@RequestParam String name) {
        return ResponseEntity.ok(borrowService.getBorrowsByMemberName(name)
                .stream()
                .map((borrow -> new BorrowedBookDto(
                        borrow.getBook().getTitle(),
                        borrow.getBook().getAuthor(),
                        borrow.getBorrowDate(),
                        borrow.getReturnDate(),
                        borrow.isReturned()
                )))
                .toList()
        );
    }

    @GetMapping("/distinct-names")
    @Operation(summary = "Get all distinct borrowed book titles")
    public ResponseEntity<List<String>> getDistinctBookTitles() {
        return ResponseEntity.ok(borrowService.getAllDistinctBorrowedBookTitles());
    }

    @GetMapping("/distinct-names-with-count")
    @Operation(summary = "Get all distinct borrowed book titles with how many copies were borrowed")
    public ResponseEntity<List<Map<String, Object>>> getDistinctTitlesWithCount() {
        List<Object[]> raw = borrowService.getAllBorrowedBookTitlesWithCount();
        List<Map<String, Object>> result = raw.stream()
                .map(obj -> Map.of("title", obj[0], "count", obj[1]))
                .toList();
        return ResponseEntity.ok(result);
    }

    private BorrowResponseDto toDto(Borrow borrow) {
        return new BorrowResponseDto(
                borrow.getId(),
                borrow.getBook().getId(),
                borrow.getMember().getId(),
                borrow.getBorrowDate(),
                borrow.getReturnDate(),
                borrow.isReturned()
        );
    }
}
