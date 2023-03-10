package com.dangdang.server.controller.member;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dangdang.server.domain.member.application.SmsMessageService;
import com.dangdang.server.domain.member.domain.MemberRepository;
import com.dangdang.server.domain.member.domain.RedisAuthCodeRepository;
import com.dangdang.server.domain.member.domain.RedisSmsTenRepository;
import com.dangdang.server.domain.member.domain.entity.Member;
import com.dangdang.server.domain.member.domain.entity.RedisAuthCode;
import com.dangdang.server.domain.member.dto.request.MemberRefreshRequest;
import com.dangdang.server.domain.member.dto.request.MemberSignUpRequest;
import com.dangdang.server.domain.member.dto.request.PhoneNumberCertifyRequest;
import com.dangdang.server.domain.member.dto.request.SmsRequest;
import com.dangdang.server.global.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
class MemberRestDocsTest {

  @Autowired
  MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  SmsMessageService smsMessageService;
  @Autowired
  MemberRepository memberRepository;
  @Autowired
  RedisAuthCodeRepository redisAuthCodeRepository;
  @Autowired
  JwtTokenProvider jwtTokenProvider;
  @Autowired
  RedisSmsTenRepository redisSmsTenRepository;

  @Test
  @DisplayName("?????? ?????? ??? ????????? ????????? ?????? ????????? ?????????, ?????? ????????? ?????? ?????? ????????? Http 200 ??????????????? ?????????")
  void signupCertifyTest() throws Exception {
    //given
    //?????? ?????? ??????
    String phoneNumber = "01233412344";
    SmsRequest smsRequest = new SmsRequest(phoneNumber);
    redisSmsTenRepository.deleteAll();

    String authCode = smsMessageService.sendMessage(smsRequest);

    //?????? ??????
    PhoneNumberCertifyRequest phoneNumberCertifyRequest = new PhoneNumberCertifyRequest(phoneNumber,
        authCode);

    String json = objectMapper.writeValueAsString(phoneNumberCertifyRequest);
    //when
    //then
    mockMvc.perform(
            post("/members/signup-certify")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(json)
        )
        .andExpect(status().isOk())
        .andExpect(result -> jsonPath("accessToken").value(null))
        .andExpect(result -> jsonPath("isCertified").value(true))
        .andDo(
            document(
                "MemberController/signupCertify",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                    fieldWithPath("phoneNumber").type(JsonFieldType.STRING)
                        .description("?????? ????????? ?????? ??????"),
                    fieldWithPath("authCode").type(JsonFieldType.STRING).description("?????? ??????")
                ),
                responseFields(
                    fieldWithPath("accessToken").type(JsonFieldType.NULL)
                        .description("accessToken"),
                    fieldWithPath("refreshToken").type(JsonFieldType.NULL).description("???????????? ??????"),
                    fieldWithPath("isCertified").type(JsonFieldType.BOOLEAN).description("?????? ??????")
                )
            )
        );
  }

  @Test
  @DisplayName("????????? ??? ????????? ????????? ?????? ????????? ???????????? ??????, ?????? ??? http 200 status code??? accessToken ??? ?????????")
  void loginCertifyTest() throws Exception {
    //?????? ????????? ?????? ??????
    long id = 1L;
    String phoneNuber = "01233412344";
    String nickname = "cloudwi";
    Member member = new Member(1L, phoneNuber, nickname);
    memberRepository.save(member);

    //?????? ?????? ??????
    SmsRequest smsRequest = new SmsRequest(phoneNuber);
    String authCode = smsMessageService.sendMessage(smsRequest);

    PhoneNumberCertifyRequest phoneNumberCertifyRequest = new PhoneNumberCertifyRequest(phoneNuber,
        authCode);

    String json = objectMapper.writeValueAsString(phoneNumberCertifyRequest);

    mockMvc.perform(
            post("/members/login-certify")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(json)
        )
        .andExpect(status().isOk())
        .andDo(
            document(
                "MemberController/loginCertify",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                    fieldWithPath("phoneNumber").type(JsonFieldType.STRING)
                        .description("?????? ????????? ?????? ??????"),
                    fieldWithPath("authCode").type(JsonFieldType.STRING).description("?????? ??????")
                ),
                responseFields(
                    fieldWithPath("accessToken").type(JsonFieldType.STRING)
                        .description("accessToken"),
                    fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("???????????? ??????"),
                    fieldWithPath("isCertified").type(JsonFieldType.BOOLEAN).description("?????? ??????")
                )
            )
        );
  }

  @Test
  @DisplayName("/api/v1/signup -> ???????????? ????????? ????????? ????????? ?????? ?????? ???????????? ??????, ?????? ??? http 200 status code??? accessToken?????? ??????")
  void signUpTest() throws Exception {
    //?????? ????????? ?????? ??????
    long id = 1L;
    String phoneNuber = "01233412344";

    //????????? ?????? ?????? ??????
    redisAuthCodeRepository.save(new RedisAuthCode(phoneNuber));

    //requestDto ??????
    String townName = "?????????";
    String nickname = "cloudwi";

    MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(townName, phoneNuber,
        nickname);

    String json = objectMapper.writeValueAsString(memberSignUpRequest);

    mockMvc.perform(
            post("/members/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(json)
        )
        .andExpect(status().isOk())
        .andDo(
            document(
                "MemberController/signup",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                    fieldWithPath("townName").type(JsonFieldType.STRING).description("??? ?????? ?????? ??????"),
                    fieldWithPath("phoneNumber").type(JsonFieldType.STRING)
                        .description("?????? ????????? ?????? ??????"),
                    fieldWithPath("profileImgUrl").type(JsonFieldType.NULL)
                        .description("????????? ???????????? Optional"),
                    fieldWithPath("nickname").type(JsonFieldType.STRING).description("?????? ??????")
                ),
                responseFields(
                    fieldWithPath("accessToken").type(JsonFieldType.STRING)
                        .description("accessToken"),
                    fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("???????????? ??????"),
                    fieldWithPath("isCertified").type(JsonFieldType.BOOLEAN).description("?????? ??????")
                )
            )
        );
  }

  @Test
  @DisplayName("/api/v1/refresh -> ????????? ???????????? ???????????? 2?????? ????????? ??? ?????? ?????? ??? ??????.")
  void refresh() throws Exception {
    //?????? ????????? ?????? ??????
    Member member = new Member("01233412344", "cloudwi");
    Member save = memberRepository.save(member);

    String refreshToken = jwtTokenProvider.createRefreshToken(save.getId());
    member.refresh(refreshToken);

    MemberRefreshRequest memberRefreshRequest = new MemberRefreshRequest(refreshToken);

    String json = objectMapper.writeValueAsString(memberRefreshRequest);

    mockMvc.perform(
            post("/members/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(json)
        )
        .andExpect(status().isOk())
        .andDo(
            document(
                "MemberController/signup",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                    fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("???????????? ??????")
                ),
                responseFields(
                    fieldWithPath("accessToken").type(JsonFieldType.STRING)
                        .description("accessToken"),
                    fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("???????????? ??????"),
                    fieldWithPath("isCertified").type(JsonFieldType.BOOLEAN).description("?????? ??????")
                )
            )
        );
  }

  @Test
  @DisplayName("???????????? restdoce test")
  void logout() throws Exception {
    //?????? ????????? ?????? ??????
    Member member = new Member("01233412344", "cloudwi");
    member = memberRepository.save(member);

    //accessToken create
    String accessToken = "Bearer " + jwtTokenProvider.createAccessToken(member.getId());

    mockMvc.perform(
            delete("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .header("AccessToken", accessToken)
        )
        .andExpect(status().isOk())
        .andDo(
            document("MemberController/logout",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("AccessToken").description("accessToken")
                )
            )
        );
  }
}
