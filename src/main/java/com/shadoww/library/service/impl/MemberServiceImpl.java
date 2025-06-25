package com.shadoww.library.service.impl;

import com.shadoww.library.model.Member;
import com.shadoww.library.repository.BorrowRepository;
import com.shadoww.library.repository.MemberRepository;
import com.shadoww.library.service.MemberService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final BorrowRepository borrowRepository;

    @Override
    @Transactional
    public Member create(Member member) {

        validate(member);

        return memberRepository.save(member);
    }

    @Override
    @Transactional
    public Member update(Long id, Member updated) {

        validate(updated);

        Member existingMember = findById(id);

        existingMember.setName(updated.getName());

        return memberRepository.save(existingMember);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        Member existingMember = findById(id);

        if (!borrowRepository.findByMemberAndReturnedFalse(existingMember).isEmpty()) {
            throw new IllegalStateException("Cannot delete member with borrowed books");
        }

        memberRepository.delete(existingMember);
    }

    @Override
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + id));
    }

    @Override
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    private void validate(Member member) {
        if(Objects.isNull(member)) {
            throw new IllegalArgumentException("Member cannot be null");
        }

        if(Objects.isNull(member.getName()) || member.getName().isEmpty()) {
            throw new IllegalArgumentException("Member must have name");
        }

    }
}
