package com.shadoww.library.service.impl;

import com.shadoww.library.model.Borrow;
import com.shadoww.library.model.Member;
import com.shadoww.library.repository.BorrowRepository;
import com.shadoww.library.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MemberServiceImplTests {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private BorrowRepository borrowRepository;

    private MemberServiceImpl memberService;

    private final Long memberId = 1L;
    private final String name = "John";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        memberService = new MemberServiceImpl(memberRepository, borrowRepository);
    }

    // === create ===

    @Test
    void create_shouldSucceed_whenValid() {
        Member member = new Member();
        member.setName(name);

        when(memberRepository.save(member)).thenReturn(member);

        Member result = memberService.create(member);

        assertThat(result).isEqualTo(member);
    }

    @Test
    void create_shouldThrow_whenNull() {
        assertThrows(IllegalArgumentException.class, () -> memberService.create(null));
    }

    @Test
    void create_shouldThrow_whenNameMissing() {
        Member member = new Member(); // name = null

        assertThrows(IllegalArgumentException.class, () -> memberService.create(member));
    }

    @Test
    void create_shouldThrow_whenNameEmpty() {
        Member member = new Member();
        member.setName("");

        assertThrows(IllegalArgumentException.class, () -> memberService.create(member));
    }

    // === update ===

    @Test
    void update_shouldSucceed_whenValid() {
        Member existing = new Member();
        existing.setId(memberId);
        existing.setName("Old Name");

        Member updated = new Member();
        updated.setName(name);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existing));
        when(memberRepository.save(existing)).thenReturn(existing);

        Member result = memberService.update(memberId, updated);

        assertThat(result.getName()).isEqualTo(name);
    }

    @Test
    void update_shouldThrow_whenMemberNotFound() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        Member updated = new Member();
        updated.setName(name);

        assertThrows(EntityNotFoundException.class, () -> memberService.update(memberId, updated));
    }

    @Test
    void update_shouldThrow_whenInvalidMember() {
        Member invalid = new Member(); // no name

        assertThrows(IllegalArgumentException.class, () -> memberService.update(memberId, invalid));
    }

    // === delete ===

    @Test
    void delete_shouldSucceed_whenNoBorrows() {
        Member member = new Member();
        member.setId(memberId);
        member.setName(name);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(borrowRepository.findByMemberAndReturnedFalse(member)).thenReturn(Collections.emptyList());

        memberService.delete(memberId);

        verify(memberRepository).delete(member);
    }

    @Test
    void delete_shouldThrow_whenHasActiveBorrows() {
        Member member = new Member();
        member.setId(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(borrowRepository.findByMemberAndReturnedFalse(member)).thenReturn(List.of(new Borrow()));

        assertThrows(IllegalStateException.class, () -> memberService.delete(memberId));
    }

    @Test
    void delete_shouldThrow_whenMemberNotFound() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> memberService.delete(memberId));
    }

    // === findById / findAll ===

    @Test
    void findById_shouldReturn_whenExists() {
        Member member = new Member();
        member.setId(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        Member result = memberService.findById(memberId);

        assertThat(result).isEqualTo(member);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> memberService.findById(memberId));
    }

    @Test
    void findAll_shouldReturnList() {
        List<Member> members = List.of(new Member(), new Member());

        when(memberRepository.findAll()).thenReturn(members);

        List<Member> result = memberService.findAll();

        assertThat(result).isEqualTo(members);
    }
}
