package com.example.communityservice.global.exception;

/** 댓글을 찾을 수 없을 때 발생하는 예외 */
public class CommentNotFoundException extends BusinessException {

  public CommentNotFoundException() {
    super(ErrorCode.COMMENT_NOT_FOUND);
  }

  public CommentNotFoundException(String message) {
    super(ErrorCode.COMMENT_NOT_FOUND, message);
  }
}
