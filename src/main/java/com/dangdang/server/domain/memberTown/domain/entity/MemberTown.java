package com.dangdang.server.domain.memberTown.domain.entity;

import static com.dangdang.server.domain.memberTown.domain.entity.TownAuthStatus.TOWN_UNCERTIFIED;

import com.dangdang.server.domain.common.BaseEntity;
import com.dangdang.server.domain.common.StatusType;
import com.dangdang.server.domain.member.domain.entity.Member;
import com.dangdang.server.domain.town.domain.entity.Town;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Getter;

@Getter
@Entity
public class MemberTown extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_town_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "town_id")
  private Town town;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RangeType rangeType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TownAuthStatus townAuthStatus;

  protected MemberTown() {
  }

  public MemberTown(Member member, Town town) {
    this.member = member;
    this.town = town;
    this.rangeType = RangeType.LEVEL2;
    this.status = StatusType.ACTIVE;
    this.townAuthStatus = TOWN_UNCERTIFIED;
  }



  public MemberTown(Long id, Member member, Town town, RangeType rangeType,
      TownAuthStatus townAuthStatus) {
    this.id = id;
    this.member = member;
    this.town = town;
    this.rangeType = rangeType;
    this.townAuthStatus = townAuthStatus;
  }

  public String getTownName() {
    return this.getTown().getName();
  }

  public void updateMemberTownStatus(StatusType statusType) {
    this.status = statusType;
  }

  public void updateMemberTownRange(RangeType rangeType) {
    this.rangeType = rangeType;
  }

  public void updateMemberTownAuthStatus(TownAuthStatus authStatus) {
    this.townAuthStatus = authStatus;
  }
}
