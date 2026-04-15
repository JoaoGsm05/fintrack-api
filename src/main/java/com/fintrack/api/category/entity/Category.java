package com.fintrack.api.category.entity;

import com.fintrack.api.shared.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** ID da categoria pai — null indica categoria raiz. */
    @Column(name = "parent_id")
    private UUID parentId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String icon;

    /** Código hexadecimal da cor, ex: #FF5733 */
    @Column(length = 7)
    private String color;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
