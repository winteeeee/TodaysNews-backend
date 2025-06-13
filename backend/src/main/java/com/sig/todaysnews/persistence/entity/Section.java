package com.sig.todaysnews.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "section")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sectionId;
    private String sectionName;
}
