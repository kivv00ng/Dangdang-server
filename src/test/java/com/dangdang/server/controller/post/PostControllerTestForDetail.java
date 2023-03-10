package com.dangdang.server.controller.post;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dangdang.server.domain.member.domain.MemberRepository;
import com.dangdang.server.domain.member.domain.entity.Member;
import com.dangdang.server.domain.memberTown.domain.MemberTownRepository;
import com.dangdang.server.domain.memberTown.domain.entity.MemberTown;
import com.dangdang.server.domain.post.application.PostService;
import com.dangdang.server.domain.post.domain.Category;
import com.dangdang.server.domain.post.domain.PostRepository;
import com.dangdang.server.domain.post.dto.request.PostSaveRequest;
import com.dangdang.server.domain.post.dto.response.PostDetailResponse;
import com.dangdang.server.domain.postImage.domain.PostImageRepository;
import com.dangdang.server.domain.postImage.dto.PostImageRequest;
import com.dangdang.server.domain.town.domain.TownRepository;
import com.dangdang.server.domain.town.domain.entity.Town;
import com.dangdang.server.global.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@SpringBootTest
class PostControllerTestForDetail {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  PostService postService;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  private SaveClassForViewUpdate saveClassForViewUpdate;

  @TestConfiguration
  static class testConfig {

    @Bean
    public SaveClassForViewUpdate innerClass() {
      return new SaveClassForViewUpdate();
    }
  }

  static class SaveClassForViewUpdate {

    @Autowired
    PostService postService;
    @Autowired
    PostRepository postRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TownRepository townRepository;
    @Autowired
    MemberTownRepository memberTownRepository;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    PostImageRepository postImageRepository;

    Member innerMember;
    Town innerTown;
    MemberTown innerMemberTown;
    PostSaveRequest postSaveRequest;
    PostDetailResponse postDetailResponse;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save() {
      Member newMember = new Member("01098764470", "testImgUrl", "????????? ??????");
      innerMember = memberRepository.save(newMember);
      innerTown = townRepository.findByName("?????????").get();

      MemberTown newMemberTown = new MemberTown(innerMember, innerTown);
      innerMemberTown = memberTownRepository.save(newMemberTown);

      postSaveRequest = new PostSaveRequest("????????? ??????", "????????? ??????", Category.???????????????,
          20000, "????????? ????????????",
          BigDecimal.valueOf(127.0000), BigDecimal.valueOf(36.0000), false, "?????????",
          new PostImageRequest(new ArrayList<>()));

      postDetailResponse = postService.savePost(postSaveRequest, innerMember.getId());
    }

    public PostSaveRequest getPosSaveRequest() {
      return postSaveRequest;
    }

    public PostDetailResponse getPostDetailResponse() {
      return postDetailResponse;
    }

    public String getAccessToken() {
      return "Bearer " + jwtTokenProvider.createAccessToken(innerMember.getId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAfterTest() {
      memberRepository.deleteById(innerMember.getId());
      memberTownRepository.deleteById(innerMemberTown.getId());
      postRepository.deleteById(postDetailResponse.postId());
    }
  }

  @Test
  @DisplayName("????????? ?????? ????????? ????????? ??? ??????.")
  public void findPostDetailTest() {
    try {
      saveClassForViewUpdate.save();
      String accessToken = saveClassForViewUpdate.getAccessToken();
      PostDetailResponse postDetailResponse = saveClassForViewUpdate.getPostDetailResponse();

      mockMvc.perform(
              RestDocumentationRequestBuilders.get("/posts/{id}", postDetailResponse.postId())
                  .header("AccessToken", accessToken))
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andDo(print())
          .andDo(document("post/api/get/findById", preprocessResponse(prettyPrint()),
              requestHeaders(headerWithName("AccessToken").description("Access Token")),
              responseFields(
                  fieldWithPath("postResponse.id").type(JsonFieldType.NUMBER)
                      .description("????????? ?????????"),
                  fieldWithPath("postResponse.title").type(JsonFieldType.STRING)
                      .description("????????? ??????"),
                  fieldWithPath("postResponse.content").type(JsonFieldType.STRING)
                      .description("????????? ??????"),
                  fieldWithPath("postResponse.category").type(JsonFieldType.STRING)
                      .description("????????????"),
                  fieldWithPath("postResponse.price").type(JsonFieldType.NUMBER).description("??????"),
                  fieldWithPath("postResponse.desiredPlaceName").type(JsonFieldType.STRING)
                      .description("???????????? ?????? ??????").optional(),
                  fieldWithPath("postResponse.desiredPlaceLongitude").type(JsonFieldType.NUMBER)
                      .description("???????????? ?????? ??????").optional(),
                  fieldWithPath("postResponse.desiredPlaceLatitude").type(JsonFieldType.NUMBER)
                      .description("???????????? ?????? ??????").optional(),
                  fieldWithPath("postResponse.view").type(JsonFieldType.NUMBER).description("?????????"),
                  fieldWithPath("postResponse.sharing").type(JsonFieldType.BOOLEAN)
                      .description("?????? ??????"),
                  fieldWithPath("postResponse.townName").type(JsonFieldType.STRING)
                      .description("?????? ?????? ??????"),
                  fieldWithPath("postResponse.statusType").type(JsonFieldType.STRING)
                      .description("?????? ??????"),
                  fieldWithPath("postResponse.likeCount").type(JsonFieldType.NUMBER)
                      .description("????????? ??????"),
                  fieldWithPath("memberResponse.id").type(JsonFieldType.NUMBER)
                      .description("????????? ?????????"),
                  fieldWithPath("memberResponse.profileImgUrl").type(JsonFieldType.STRING)
                      .description("????????? ????????? ????????? url").optional(),
                  fieldWithPath("memberResponse.nickName").type(JsonFieldType.STRING)
                      .description("????????? ?????????"),
                  fieldWithPath("imageUrls").type(JsonFieldType.ARRAY)
                      .description("????????? ????????? url ?????????")
                      .optional())));
    } catch (Exception exception) {
      log.info("########+ ????????????" + exception.getMessage());
    } finally {
      saveClassForViewUpdate.deleteAfterTest();
    }
  }
}