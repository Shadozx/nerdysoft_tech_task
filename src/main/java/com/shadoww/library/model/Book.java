package com.shadoww.library.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Book {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank
    @Size(min = 3, message = "Title must be at least 3 characters")
    private String title;


    @NotBlank(message = "Author is required")
    @Pattern(
            regexp = "^[A-Z][a-z]+\\s[A-Z][a-z]+$",
            message = "Author must be in format 'Name Surname'"
    )
    private String author;

    @Min(value = 0, message = "Amount must be 0 or greater")
    private int amount = 1;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonIgnore
    private Set<Borrow> borrows = new HashSet<>();
}