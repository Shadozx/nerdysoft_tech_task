package com.shadoww.library.service;


import com.shadoww.library.model.Member;

import java.util.List;

public interface MemberService {

    Member create(Member member);

    Member update(Long id, Member updated);

    void delete(Long id);

    Member findById(Long id);

    List<Member> findAll();
}