package com.dangdang.server.controller.memberTown;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dangdang.server.domain.common.StatusType;
import com.dangdang.server.domain.member.domain.MemberRepository;
import com.dangdang.server.domain.member.domain.entity.Member;
import com.dangdang.server.domain.memberTown.domain.MemberTownRepository;
import com.dangdang.server.domain.memberTown.domain.entity.MemberTown;
import com.dangdang.server.domain.memberTown.dto.request.MemberTownCertifyRequest;
import com.dangdang.server.domain.memberTown.dto.request.MemberTownRangeRequest;
import com.dangdang.server.domain.memberTown.dto.request.MemberTownRequest;
import com.dangdang.server.domain.town.domain.TownRepository;
import com.dangdang.server.domain.town.domain.entity.Town;
import com.dangdang.server.global.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class MemberTownControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  TownRepository townRepository;

  @Autowired
  MemberTownRepository memberTownRepository;

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  JwtTokenProvider jwtTokenProvider;

  Member member;
  String accessToken;

  @BeforeEach
  void setup() {
    member = new Member("01012345678", null, "Albatross");
    Member save = memberRepository.save(member);
    accessToken = "Bearer " + jwtTokenProvider.createAccessToken(save.getId());
  }

  @AfterEach
  void clear() {
    memberRepository.deleteById(member.getId());
  }

  @Test
  @DisplayName("?????? ?????? ?????? ??????")
  @Transactional
  void createMemberTown() throws Exception {
    // given
    // member-town 1??? ???????????? ????????? (????????? ??? ?????? ?????? ??????)
    Town existingTown = townRepository.findByName("??????2???").get();
    MemberTown existingMemberTown = new MemberTown(member, existingTown);
    memberTownRepository.save(existingMemberTown);

    MemberTownRequest memberTownRequest = new MemberTownRequest("??????1???");

    mockMvc.perform(post("/member-town")
            .header("AccessToken", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(memberTownRequest)))
        .andExpect(status().isOk())
        .andDo(
            document("api/v1/post/member-town",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("AccessToken").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("townName").description("?????? ??????")
                ),
                responseFields(
                    fieldWithPath("townName").description("?????? ??????")
                )
            ));
  }

  @Test
  @DisplayName("?????? ?????? ?????? ?????? - member town 1?????? ?????? ??????")
  @Transactional
  void createMemberTown_fail() throws Exception {
    MemberTownRequest memberTownRequest = new MemberTownRequest("??????1???");

    mockMvc.perform(post("/member-town")
            .header("AccessToken", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(memberTownRequest)))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @Test
  @DisplayName("?????? ?????? ?????? ?????? - town ?????? ??????")
  @Transactional
  void createMemberTown_fail_byNotFoundTown() throws Exception {
    // given
    // member-town 1??? ???????????? ????????? (????????? ??? ?????? ?????? ??????)
    Town existingTown = townRepository.findByName("??????2???").get();
    MemberTown existingMemberTown = new MemberTown(member, existingTown);
    memberTownRepository.save(existingMemberTown);
    MemberTownRequest memberTownRequest = new MemberTownRequest("??????7???");

    mockMvc.perform(post("/member-town")
            .header("AccessToken", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(memberTownRequest)))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("?????? ?????? ?????? ??????")
  @Transactional
  void deleteMemberTown() throws Exception {
    // given
    // member-town 2??? ????????? ??? (existingMemberTown1??? Inactive ??? ??????)
    Town existingTown1 = townRepository.findByName("??????1???").get();
    MemberTown existingMemberTown1 = new MemberTown(member, existingTown1);
    memberTownRepository.save(existingMemberTown1);

    Town existingTown2 = townRepository.findByName("??????2???").get();
    MemberTown existingMemberTown2 = new MemberTown(member, existingTown2);
    memberTownRepository.save(existingMemberTown2);

    MemberTownRequest memberTownRequest = new MemberTownRequest("??????2???");

    mockMvc.perform(delete("/member-town")
            .header("AccessToken", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(memberTownRequest)))
        .andExpect(status().isOk())
        .andDo(
            document("api/v1/delete/member-town",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("AccessToken").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("townName").description("?????? ??????")
                )
            ));

  }

  @Test
  @DisplayName("?????? ?????? ?????? ?????? - ?????? ?????? ????????? 2?????? ?????? ??????")
  void deleteMemberTown_fail_byMemberTownSizeNot2() throws Exception {
    MemberTownRequest memberTownRequest = new MemberTownRequest("??????2???");

    mockMvc.perform(delete("/member-town")
            .header("AccessToken", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(memberTownRequest)))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @Test
  @DisplayName("?????? ?????? ?????? ?????? - ????????? ?????? ?????? ????????? ??????")
  @Transactional
  void deleteMemberTown_fail_byWrongMemberTownName() throws Exception {
    Town existingTown1 = townRepository.findByName("??????1???").get();
    MemberTown existingMemberTown1 = new MemberTown(member, existingTown1);
    memberTownRepository.save(existingMemberTown1);

    Town existingTown2 = townRepository.findByName("??????2???").get();
    MemberTown existingMemberTown2 = new MemberTown(member, existingTown2);
    memberTownRepository.save(existingMemberTown2);

    MemberTownRequest memberTownRequest = new MemberTownRequest("??????7???");

    mockMvc.perform(delete("/member-town")
            .header("AccessToken", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(memberTownRequest)))
        .andExpect(status().isNotFound())
        .andDo(print());
  }

  @Test
  @DisplayName("?????? ?????? ????????? ?????? ??????")
  @Transactional
  void changeActiveMemberTown() throws Exception {
    // given
    Town existingTown1 = townRepository.findByName("??????1???").get();
    MemberTown existingMemberTown1 = new MemberTown(member, existingTown1);
    existingMemberTown1.updateMemberTownStatus(StatusType.INACTIVE);
    memberTownRepository.save(existingMemberTown1);

    Town existingTown2 = townRepository.findByName("??????2???").get();
    MemberTown existingMemberTown2 = new MemberTown(member, existingTown2);
    memberTownRepository.save(existingMemberTown2);

    // ?????? 1????????? Active ?????? ??????
    MemberTownRequest memberTownRequest = new MemberTownRequest("??????1???");

    mockMvc.perform(put("/member-town/active")
            .header("AccessToken", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(memberTownRequest)))
        .andExpect(status().isOk())
        .andDo(
            document("api/v1/put/member-town/active",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("AccessToken").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("townName").description("?????? ??????")
                ),
                responseFields(
                    fieldWithPath("townName").description("?????? ??????")
                )
            ));
  }

  @Test
  @DisplayName("?????? ?????? ????????? ?????? ?????? - ?????? ?????? ????????? 2?????? ?????? ??????")
  @Transactional
  void changeActiveMemberTown_fail_byMemberTownSizeNot2() throws Exception {
    Town existingTown1 = townRepository.findByName("??????1???").get();
    MemberTown existingMemberTown1 = new MemberTown(member, existingTown1);
    memberTownRepository.save(existingMemberTown1);

    MemberTownRequest memberTownRequest = new MemberTownRequest("??????2???");

    mockMvc.perform(put("/member-town/active")
            .header("AccessToken", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(memberTownRequest)))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @Test
  @DisplayName("?????? ?????? ?????? ?????? ??????")
  @Transactional
  void changeMemberTownRange() throws Exception {
    // given
    Town existingTown1 = townRepository.findByName("??????1???").get();
    MemberTown existingMemberTown1 = new MemberTown(member, existingTown1);
    memberTownRepository.save(existingMemberTown1);

    MemberTownRangeRequest memberTownRangeRequest = new MemberTownRangeRequest("??????1???", 3);

    mockMvc.perform(put("/member-town/range")
            .header("AccessToken", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(memberTownRangeRequest)))
        .andExpect(status().isOk())
        .andDo(
            document("api/v1/put/member-town/range",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("AccessToken").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("townName").description("?????? ??????"),
                    fieldWithPath("level").description("?????? ?????? ??????[1-4]")
                ),
                responseFields(
                    fieldWithPath("townName").description("?????? ??????"),
                    fieldWithPath("level").description("?????? ?????? ??????[1-4]")
                )
            ));
  }

  @Test
  @DisplayName("?????? ?????? range ?????? ?????? - range ????????? ????????? ??????")
  @Transactional
  void changeMemberTownRange_fail_byWrongMemberTownRange() throws Exception {
    // given
    Town existingTown1 = townRepository.findByName("??????1???").get();
    MemberTown existingMemberTown1 = new MemberTown(member, existingTown1);
    memberTownRepository.save(existingMemberTown1);

    MemberTownRangeRequest memberTownRangeRequest = new MemberTownRangeRequest("??????1???", 7);

    mockMvc.perform(put("/member-town/range")
            .header("AccessToken", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(memberTownRangeRequest)))
        .andExpect(status().isBadRequest())
        .andDo(print());

  }

  @Test
  @DisplayName("??? ?????? ?????? ??????")
  @Transactional
  void certifyMemberTown_success() throws Exception {
    // given
    Town existingTown = townRepository.findByName("??????1???").get();
    MemberTown memberTown = new MemberTown(member, existingTown);
    memberTownRepository.save(memberTown);

    MemberTownCertifyRequest memberTownCertifyRequest = new MemberTownCertifyRequest(
        BigDecimal.valueOf(127.0738380000), BigDecimal.valueOf(37.6248740000));

    mockMvc.perform(post("/member-town/certification")
            .header("AccessToken", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(memberTownCertifyRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isCertified", equalTo(true)))
        .andDo(
            document("api/v1/post/member-town/certification",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("AccessToken").description("Access Token")
                ),
                requestFields(
                    fieldWithPath("longitude").description("??????"),
                    fieldWithPath("latitude").description("??????")
                ),
                responseFields(
                    fieldWithPath("isCertified").description("??? ?????? ?????? ??????")
                )
            ));
  }

    @Test
    @DisplayName("??? ?????? ?????? ?????? - ??? ?????? ????????? ??? ????????? ?????? ??????")
    @Transactional
    void certifyMemberTown_fail_byMemberTownNotInMyPositionRanges() throws Exception {
      // given
      Town existingTown = townRepository.findByName("??????1???").get();
      MemberTown memberTown = new MemberTown(member, existingTown);
      memberTownRepository.save(memberTown);

      MemberTownCertifyRequest memberTownCertifyRequest = new MemberTownCertifyRequest(
          BigDecimal.valueOf(127.0625320000), BigDecimal.valueOf(37.5144424000));

      mockMvc.perform(post("/member-town/certification")
              .header("AccessToken", accessToken)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(memberTownCertifyRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isCertified", equalTo(false)))
          .andDo(print());
  }

  @Test
  @DisplayName("??? ?????? ?????? ?????? - Active ????????? ??? ????????? ?????? ??????")
  @Transactional
  void certifyMemberTown_fail_byNotSettingActiveMemberTown() throws Exception {
    // given
    Town existingTown = townRepository.findByName("??????1???").get();
    MemberTown memberTown = new MemberTown(member, existingTown);
    memberTown.updateMemberTownStatus(StatusType.INACTIVE);
    memberTownRepository.save(memberTown);

    MemberTownCertifyRequest memberTownCertifyRequest = new MemberTownCertifyRequest(
        BigDecimal.valueOf(127.0625320000), BigDecimal.valueOf(37.5144424000));

    mockMvc.perform(post("/member-town/certification")
            .header("AccessToken", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(memberTownCertifyRequest)))
        .andExpect(status().isNotFound())
        .andDo(print());
  }
}