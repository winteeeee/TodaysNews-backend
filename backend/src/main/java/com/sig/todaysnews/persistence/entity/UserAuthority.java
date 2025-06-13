package com.sig.todaysnews.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_authority")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "authority_name")
    private String authorityName;
}
