package com.rc.readcompass.storage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rc.readcompass.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tb_binary_content")
@Getter
@SuperBuilder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BinaryContent extends BaseEntity {

    @Column(updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID bookId;

    @Column(nullable = false, length = 255)
    private String originFileUrl;

    @Column(nullable = false, length = 255)
    private String renamedFileUrl;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false, length = 100)
    private String contentType;
}