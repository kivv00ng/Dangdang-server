package com.dangdang.server.domain.member.exception;

import com.dangdang.server.global.exception.BusinessException;
import com.dangdang.server.global.exception.ExceptionCode;

public class MemberCertifiedFailException extends BusinessException {

  public MemberCertifiedFailException(
      ExceptionCode exceptionCode) {
    super(exceptionCode);
  }
}
