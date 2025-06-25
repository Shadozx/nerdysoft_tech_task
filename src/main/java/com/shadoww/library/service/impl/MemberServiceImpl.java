package com.shadoww.library.service.impl;

import com.shadoww.library.model.Member;
import com.shadoww.library.repository.BorrowRepository;
import com.shadoww.library.repository.MemberRepository;
import com.shadoww.library.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final BorrowRepository borrowRepository;

    @Override
    public Member create(Member member) {

        validate(member);

        return memberRepository.save(member);
    }

    @Override
    public Member update(Long id, Member updated) {

        validate(updated);

        Member existingMember = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));

        existingMember.setName(updated.getName());

        return memberRepository.save(existingMember);
    }

    @Override
    public void delete(Long id) {

        Member existingMember = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));

        if (!borrowRepository.findByMemberAndReturnedFalse(existingMember).isEmpty()) {
            throw new IllegalStateException("Cannot delete member with borrowed books");
        }

        memberRepository.delete(existingMember);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    @Override
    public Optional<Member> findByName(String name) {
        return memberRepository.findByNameIgnoreCase(name);
    }

    @Override
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    private void validate(Member member) {
        if(member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }

        if(Objects.isNull(member.getName()) || member.getName().isEmpty()) {
            throw new IllegalArgumentException("Member cannot be null");
        }

    }
}
