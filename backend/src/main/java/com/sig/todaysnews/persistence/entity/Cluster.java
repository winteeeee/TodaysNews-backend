package com.sig.todaysnews.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cluster")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cluster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cluster_id")
    private Long clusterId;
    private LocalDate regdate;
    private String imgUrl;
    private String title;
    private String summary;
    private Integer size;
    private String words;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centroid_id")
    private Article centroid;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_cluster_id")
    private Cluster relatedCluster;
    @OneToMany(mappedBy = "cluster", fetch = FetchType.LAZY)
    private List<Article> articles = new ArrayList<>();
}
