package com.shadoww.library.service;


import com.shadoww.library.model.Member;

import java.util.List;
import java.util.Optional;

public interface MemberService {

    Member create(Member member);

    Member update(Long id, Member updated);

    void delete(Long id);

    Optional<Member> findById(Long id);

    Optional<Member> findByName(String name);

    List<Member> findAll();
}