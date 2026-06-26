package com.rc.readcompass.storage;

import com.rc.readcompass.book.entity.BinaryContent;
import java.util.Collection;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    // 첨부파일 다건 저장 → BinaryContent 목록 반환
    List<BinaryContent> save(List<MultipartFile> files);

    // 첨부파일 다건 삭제
    void delete(Collection<BinaryContent> files);

    // 파일 접근 URL 반환 (renamedFileName 기준)
    String getAttachFileUrl(String renamedFileName);
}
