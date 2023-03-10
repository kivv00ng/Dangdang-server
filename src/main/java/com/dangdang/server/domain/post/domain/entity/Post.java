package com.dangdang.server.domain.post.domain.entity;

import com.dangdang.server.domain.common.BaseEntity;
import com.dangdang.server.domain.common.StatusType;
import com.dangdang.server.domain.likes.domain.entity.Likes;
import com.dangdang.server.domain.member.domain.entity.Member;
import com.dangdang.server.domain.post.domain.Category;
import com.dangdang.server.domain.town.domain.entity.Town;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
public class Post extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "post_id")
  private Long id;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false, length = 1000)
  private String content;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Category category;

  @Column(columnDefinition = "INT UNSIGNED")
  private Integer price;

  @Column(length = 100)
  private String desiredPlaceName;

  @Column(precision = 18, scale = 10)
  private BigDecimal desiredPlaceLongitude;

  @Column(precision = 18, scale = 10)
  private BigDecimal desiredPlaceLatitude;

  @Column(nullable = false)
  @ColumnDefault("0")
  private Integer view = 0;

  @Column(nullable = false)
  @ColumnDefault("false")
  private Boolean sharing = false;

  //연관관계
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "town_id", nullable = false)
  private Town town;

  @Lob
  private String imageUrl;

  @OneToMany(mappedBy = "post")
  private List<Likes> likes = new ArrayList<>();

  protected Post() {
  }

  public void upView() {
    this.view++;
  }

  public Post(String title, String content, Category category, Integer price,
      String desiredPlaceName, BigDecimal desiredPlaceLongitude, BigDecimal desiredPlaceLatitude,
      Integer view, Boolean sharing, Member member, Town town, String imageUrl,
      StatusType statusType) {
    this.title = title;
    this.content = content;
    this.category = category;
    this.price = price;
    this.desiredPlaceName = desiredPlaceName;
    this.desiredPlaceLongitude = desiredPlaceLongitude;
    this.desiredPlaceLatitude = desiredPlaceLatitude;
    this.view = view;
    this.sharing = sharing;
    this.member = member;
    this.town = town;
    this.imageUrl = imageUrl;
    super.status = statusType;
  }

  public Post(String title, String content, Category category, Integer price,
      String desiredPlaceName,
      BigDecimal desiredPlaceLongitude, BigDecimal desiredPlaceLatitude, Boolean sharing,
      String imageUrl) {
    this.title = title;
    this.content = content;
    this.category = category;
    this.price = price;
    this.desiredPlaceName = desiredPlaceName;
    this.desiredPlaceLongitude = desiredPlaceLongitude;
    this.desiredPlaceLatitude = desiredPlaceLatitude;
    this.sharing = sharing;
    this.imageUrl = imageUrl;
  }

  public Long getMemberId() {
    return member.getId();
  }

  public String getTownName() {
    return town.getName();
  }

  public int getLikeCount() {
    return likes.size();
  }

  public void addLikes(Likes likes) {
    this.likes.add(likes);
  }

  public void changeStatus(StatusType statusType) {
    this.status = statusType;
  }

  public void changePost(Post post) {
    this.title = post.getTitle();
    this.content = post.getContent();
    this.category = post.getCategory();
    this.price = post.getPrice();
    this.desiredPlaceName = post.getDesiredPlaceName();
    this.desiredPlaceLongitude = post.getDesiredPlaceLongitude();
    this.desiredPlaceLatitude = post.getDesiredPlaceLatitude();
    this.sharing = post.getSharing();
    this.imageUrl = post.getImageUrl();
  }
}
