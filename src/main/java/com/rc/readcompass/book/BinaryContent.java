package com.rc.readcompass.book;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rc.readcompass.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tb_binary_content")
@Getter
@SuperBuilder
@ToString(exclude = "book")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BinaryContent extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false, length = 255)
    private String originFileUrl;

    @Column(nullable = false, length = 255)
    private String renamedFileUrl;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false, length = 100)
    private String contentType;

    // 파일 저장 후 서비스 계층에서 도서와의 연관관계를 설정한다.
    public void assignBook(Book book) {
        this.book = book;
    }
}