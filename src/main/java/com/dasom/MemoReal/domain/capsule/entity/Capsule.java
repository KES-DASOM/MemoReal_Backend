package com.dasom.MemoReal.domain.capsule.entity;

import com.dasom.MemoReal.domain.capsule.type.CapsuleType;
import com.dasom.MemoReal.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "capsule")
@EntityListeners(AuditingEntityListener.class)
public class Capsule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private CapsuleType type;

    private String content;

    @Column(nullable = false)
    private LocalDate openDate;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "capsule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> media = new ArrayList<>();

    // User 연동 추가 부분
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Capsule(String title, CapsuleType type, String content, LocalDate openDate, User user) {
        this.title = title;
        this.type = type;
        this.content = content;
        this.openDate = openDate;
        this.user = user;
    }

    public void update(String title, CapsuleType type, String content, LocalDate openDate) {
        this.title = title;
        this.type = type;
        this.content = content;
        this.openDate = openDate;
    }

    public void addMedia(Media media) {
        this.media.add(media);
        media.setCapsule(this);
    }

    public void clearMedias() {
        this.media.clear();
    }

}
