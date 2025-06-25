package com.shadoww.library.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Member {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @ManyToOne()
    @JoinColumn(name = "book_id")
    @ToString.Exclude
    @JsonIgnore
    private Book book;

    @CreationTimestamp
    private LocalDateTime membershipDate;
}
