package com.dangdang.server.controller.member;

import com.dangdang.server.domain.member.application.MemberService;
import com.dangdang.server.domain.member.dto.request.MemberRefreshRequest;
import com.dangdang.server.domain.member.dto.request.MemberSignUpRequest;
import com.dangdang.server.domain.member.dto.request.PhoneNumberCertifyRequest;
import com.dangdang.server.domain.member.dto.request.PhoneNumberVerifyRequest;
import com.dangdang.server.domain.member.dto.request.PhoneNumberVerifyResponse;
import com.dangdang.server.domain.member.dto.response.MemberCertifyResponse;
import com.dangdang.server.domain.member.exception.MemberBadRequestException;
import com.dangdang.server.global.aop.CurrentUserId;
import com.dangdang.server.global.exception.BusinessException;
import com.dangdang.server.global.exception.ExceptionCode;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
public class MemberController {

  private final MemberService memberService;

  public MemberController(MemberService memberService) {
    this.memberService = memberService;
  }

  // 완료 버튼 후 회원가입할  모든 정보 보냄
  @PostMapping("/signup")
  public ResponseEntity<MemberCertifyResponse> signUp(
      @RequestBody @Valid MemberSignUpRequest memberSignupRequest, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      throw new MemberBadRequestException(ExceptionCode.BAD_REQUEST);
    }
    MemberCertifyResponse memberCertifyResponse = memberService.signup(memberSignupRequest);
    return ResponseEntity.ok(memberCertifyResponse);
  }

  @PostMapping("/signup-certify")
  public ResponseEntity<MemberCertifyResponse> signupCertify(
      @RequestBody @Valid PhoneNumberCertifyRequest phoneNumberCertifyRequest,
      BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      throw new MemberBadRequestException(ExceptionCode.BAD_REQUEST);
    }
    MemberCertifyResponse memberCertifyResponse = memberService.signupCertify(
        phoneNumberCertifyRequest);

    return ResponseEntity.ok(memberCertifyResponse);
  }

  @PostMapping("/login-certify")
  public ResponseEntity<MemberCertifyResponse> loginCertify(
      @RequestBody @Valid PhoneNumberCertifyRequest phoneNumberCertifyRequest,
      BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      throw new MemberBadRequestException(ExceptionCode.BAD_REQUEST);
    }

    MemberCertifyResponse memberCertifyResponse = memberService.loginCertify(
        phoneNumberCertifyRequest);

    return ResponseEntity.ok(memberCertifyResponse);
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/verify")
  public PhoneNumberVerifyResponse phoneNumberVerify(
      @RequestBody @Valid PhoneNumberVerifyRequest phoneNumberVerifyRequest) {
    Long memberId = memberService.phoneNumberVerify(phoneNumberVerifyRequest);
    return new PhoneNumberVerifyResponse(memberId);
  }

  @PostMapping("/refresh")
  public ResponseEntity<MemberCertifyResponse> refresh(
      @RequestBody @Valid MemberRefreshRequest memberRefreshRequest, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      throw new BusinessException(ExceptionCode.BAD_REQUEST);
    }

    MemberCertifyResponse memberCertifyResponse = memberService.refresh(memberRefreshRequest);
    return ResponseEntity.ok(memberCertifyResponse);
  }

  @DeleteMapping
  @CurrentUserId
  public ResponseEntity logout(Long memberId) {
    memberService.logout(memberId);
    return ResponseEntity.ok().build();
  }
}
