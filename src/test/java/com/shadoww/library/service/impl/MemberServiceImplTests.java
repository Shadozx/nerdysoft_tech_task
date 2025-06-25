package com.shadoww.library.service.impl;

import com.shadoww.library.model.Borrow;
import com.shadoww.library.model.Member;
import com.shadoww.library.repository.BorrowRepository;
import com.shadoww.library.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MemberServiceImplTests {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BorrowRepository borrowRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    private final long memberId = 1L;
    private final String memberName = "John Doe";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_shouldSaveNewMember() {
        Member member = new Member();
        member.setName(memberName);

        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> {
            Member m = inv.getArgument(0);
            m.setId(memberId);
            m.setMembershipDate(LocalDateTime.now());
            return m;
        });

        Member result = memberService.create(member);

        assertThat(result.getId()).isEqualTo(memberId);
        assertThat(result.getName()).isEqualTo(memberName);
        assertThat(result.getMembershipDate()).isNotNull();
    }

    @Test
    void update_shouldChangeMemberName_whenFound() {
        Member existing = new Member();
        existing.setId(memberId);
        existing.setName("Old Name");

        Member updated = new Member();
        updated.setName("New Name");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existing));
        when(memberRepository.save(existing)).thenReturn(existing);

        Member result = memberService.update(memberId, updated);

        assertThat(result.getName()).isEqualTo("New Name");
        verify(memberRepository).save(existing);
    }

    @Test
    void update_shouldThrow_whenNotFound() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        Member updated = new Member();
        updated.setName("X");

        assertThatThrownBy(() -> memberService.update(memberId, updated))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Member not found");
    }

    @Test
    void delete_shouldRemoveMember_ifNotBorrowing() {
        Member member = new Member();
        member.setId(memberId);
        member.setName(memberName);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(borrowRepository.findByMemberAndReturnedFalse(member)).thenReturn(Collections.emptyList());

        memberService.delete(memberId);

        verify(memberRepository).delete(member);
    }

    @Test
    void delete_shouldThrow_ifBorrowingBooks() {
        Member member = new Member();
        member.setId(memberId);
        member.setName(memberName);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(borrowRepository.findByMemberAndReturnedFalse(member)).thenReturn(List.of(new Borrow()));

        assertThatThrownBy(() -> memberService.delete(memberId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("borrowed books");
    }

    @Test
    void findById_shouldReturnMember() {
        Member member = new Member();
        member.setId(memberId);
        member.setName(memberName);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        Optional<Member> result = memberService.findById(memberId);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(memberName);
    }

    @Test
    void findByName_shouldReturnMember() {
        Member member = new Member();
        member.setId(memberId);
        member.setName(memberName);

        when(memberRepository.findByNameIgnoreCase(memberName)).thenReturn(Optional.of(member));

        Optional<Member> result = memberService.findByName(memberName);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(memberId);
    }

    @Test
    void findAll_shouldReturnList() {
        when(memberRepository.findAll()).thenReturn(List.of(new Member(), new Member()));

        List<Member> result = memberService.findAll();

        assertThat(result).hasSize(2);
    }
}
