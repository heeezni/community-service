package com.example.communityservice.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_attachments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostAttachments {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "post_attachment_id")
  private Long postAttachmentId;

  @ManyToOne(fetch = FetchType.LAZY) // 지연로딩 : 필요한 순간에 추가 쿼리를 실행해서 데이터 가져오기
  @JoinColumn(name = "post_id", nullable = false)
  private Posts post;

  @Column(name = "original_filename", nullable = false)
  private String originalFilename;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "file_url", nullable = false, length = 500)
  private String fileUrl;

  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  // 생성자 팩토리 메서드

  /**
   * 첨부파일 엔티티 생성 파일 업로드 완료 후 메타데이터를 저장하기 위한 엔티티 생성
   *
   * @param post 첨부파일이 속한 게시글
   * @param originalFilename 원본 파일명
   * @param fileName 서버에 저장된 파일명 (UUID 등으로 변경된)
   * @param fileUrl 파일 접근 URL
   * @param fileSize 파일 크기 (바이트 단위)
   * @return 첨부파일 엔티티
   */
  public static PostAttachments create(
      Posts post, String originalFilename, String fileName, String fileUrl, Long fileSize) {
    return PostAttachments.builder()
        .post(post)
        .originalFilename(originalFilename)
        .fileName(fileName)
        .fileUrl(fileUrl)
        .fileSize(fileSize)
        .build();
  }

  // 파일 크기 유틸리티 메서드

  /**
   * 파일 크기를 KB 단위로 반환 일반적인 파일 크기 표시용
   *
   * @return KB 단위 파일 크기
   */
  public double getFileSizeInKB() {
    return fileSize / 1024.0;
  }

  /**
   * 파일 크기를 MB 단위로 반환 큰 파일의 크기 표시용
   *
   * @return MB 단위 파일 크기
   */
  public double getFileSizeInMB() {
    return fileSize / (1024.0 * 1024.0);
  }

  /**
   * 적절한 단위로 포맷된 파일 크기 반환 파일 크기에 따라 B/KB/MB 단위를 자동 선택하여 사용자 친화적 표시
   *
   * @return 포맷된 파일 크기 문자열 (예: "1.2 MB", "345.6 KB", "512 B")
   */
  public String getFormattedFileSize() {
    if (fileSize < 1024) { // 1KB 미만
      return fileSize + " B";
    } else if (fileSize < 1024 * 1024) { // 1MB 미만
      return String.format("%.1f KB", getFileSizeInKB());
    } else { // 1MB 이상
      return String.format("%.1f MB", getFileSizeInMB());
    }
  }
}
