package com.sig.todaysnews.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "hot_cluster")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotCluster {
    @Id
    @Column(name = "cluster_id")
    private Long cluster_id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "cluster_id")
    private Cluster cluster;
    private LocalDate regdate;
    private Integer size;
    private String roomName;
}
