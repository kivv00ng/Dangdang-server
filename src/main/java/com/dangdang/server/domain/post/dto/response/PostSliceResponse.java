package com.dangdang.server.domain.post.dto.response;

import com.dangdang.server.domain.post.domain.entity.Post;
import com.dangdang.server.global.util.S3ImageUtil;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class PostSliceResponse {

  private final Long id;
  private final String title;
  private final String townName;
  private final String imageUrl;
  private final Integer price;
  private final LocalDateTime createdAt;

  private PostSliceResponse(Long id, String title, String townName, String imageUrl, Integer price,
      LocalDateTime createdAt) {
    this.id = id;
    this.title = title;
    this.townName = townName;
    this.imageUrl = imageUrl;
    this.price = price;
    this.createdAt = createdAt;
  }

  public static PostSliceResponse from(Post post) {
    return new PostSliceResponse(post.getId(), post.getTitle(), post.getTownName(),
        S3ImageUtil.makeImageLink(post.getImageUrl()), post.getPrice(), post.getCreatedAt());
  }
}
