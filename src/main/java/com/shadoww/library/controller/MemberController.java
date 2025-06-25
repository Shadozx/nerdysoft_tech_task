package com.shadoww.library.controller;


import com.shadoww.library.dto.MemberRequestDto;
import com.shadoww.library.dto.MemberResponseDto;
import com.shadoww.library.model.Member;
import com.shadoww.library.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @Operation(summary = "Create new member")
    public ResponseEntity<MemberResponseDto> create(@RequestBody @Valid MemberRequestDto dto) {
        Member saved = memberService.create(toEntity(dto));
        return new ResponseEntity<>(toDto(saved), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all members")
    public List<MemberResponseDto> getAll() {
        return memberService.findAll().stream()
                .map(this::toDto)
                .collect(toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get member by ID")
    public ResponseEntity<MemberResponseDto> getById(@PathVariable Long id) {
        return memberService.findById(id)
                .map(m -> ResponseEntity.ok(toDto(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update member by ID")
    public ResponseEntity<MemberResponseDto> update(@PathVariable Long id,
                                                    @RequestBody @Valid MemberRequestDto dto) {
        Member updated = memberService.update(id, toEntity(dto));
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete member if no borrowed books")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // === Mapper methods ===

    private Member toEntity(MemberRequestDto dto) {
        Member member = new Member();
        member.setName(dto.name());

        return member;
    }

    private MemberResponseDto toDto(Member member) {
        return new MemberResponseDto(
                member.getId(),
                member.getName(),
                member.getMembershipDate()
        );
    }
}