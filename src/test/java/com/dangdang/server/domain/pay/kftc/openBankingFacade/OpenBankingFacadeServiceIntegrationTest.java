package com.dangdang.server.domain.pay.kftc.openBankingFacade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dangdang.server.domain.common.StatusType;
import com.dangdang.server.domain.member.domain.MemberRepository;
import com.dangdang.server.domain.member.domain.entity.Member;
import com.dangdang.server.domain.pay.banks.bankAccount.BankAccountService;
import com.dangdang.server.domain.pay.banks.bankAccount.domain.BankAccountRepository;
import com.dangdang.server.domain.pay.banks.bankAccount.domain.entity.BankAccount;
import com.dangdang.server.domain.pay.banks.bankAccount.exception.BankAccountAuthenticationException;
import com.dangdang.server.domain.pay.banks.bankAccount.exception.InactiveBankAccountException;
import com.dangdang.server.domain.pay.banks.trustAccount.application.TrustAccountService;
import com.dangdang.server.domain.pay.banks.trustAccount.domain.TrustAccountRepository;
import com.dangdang.server.domain.pay.banks.trustAccount.domain.entity.TrustAccount;
import com.dangdang.server.domain.pay.banks.trustAccount.exception.InactiveTrustAccountException;
import com.dangdang.server.domain.pay.daangnpay.domain.payMember.domain.PayMemberRepository;
import com.dangdang.server.domain.pay.daangnpay.domain.payMember.domain.entity.PayMember;
import com.dangdang.server.domain.pay.daangnpay.domain.payMember.exception.InsufficientBankAccountException;
import com.dangdang.server.domain.pay.kftc.common.dto.OpenBankingWithdrawRequest;
import com.dangdang.server.domain.pay.kftc.openBankingFacade.application.OpenBankingFacadeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DisplayName("???????????? API ???????????????")
@ActiveProfiles("internal")
class OpenBankingFacadeServiceIntegrationTest {

  @Autowired
  MemberRepository memberRepository;
  @Autowired
  PayMemberRepository payMemberRepository;
  @Autowired
  BankAccountRepository bankAccountRepository;
  @Autowired
  TrustAccountRepository trustAccountRepository;
  @Autowired
  BankAccountService bankAccountService;
  @Autowired
  TrustAccountService trustAccountService;
  @Autowired
  OpenBankingFacadeService openBankingFacadeService;

  PayMember payMember;
  TrustAccount trustAccount;
  TrustAccount trustAccountInactive;

  String fintechUseNum = "128947";
  String accountHolder = "????????????";

  @BeforeEach
  void setUp() {
    Member member = new Member("?????????", "?????????");
    memberRepository.save(member);

    payMember = new PayMember("password", member.getId());
    payMemberRepository.save(payMember);

    trustAccount = new TrustAccount("257182", 100000, "????????????_??????");
    trustAccountInactive = new TrustAccount("23947182", 100000, "????????????_??????", StatusType.INACTIVE);
    trustAccountRepository.save(trustAccountInactive);
    trustAccountRepository.save(trustAccount);
  }

  @Nested
  @DisplayName("???????????? API??? ?????? ?????? ????????? ???????????? ???")
  class withdrawTest {

    int beforeBankBalance = 0;
    int beforeTrustBalance = 0;
    BankAccount bankAccount;

    @DisplayName("?????? ?????? ????????? ????????????")
    @BeforeEach
    void setUpFromBankAccount() {
      bankAccount = new BankAccount("238471234", "????????????", 25000, payMember, "?????????");
      bankAccountRepository.save(bankAccount);
    }

    @Nested
    @DisplayName("??????")
    class whenSuccess {

      @Test
      @DisplayName("?????????????????? ?????? ???????????? ?????? ?????? ???????????? ?????? ???????????? ???????????? ??????????????? ????????????.")
      void successWithdraw() {
        beforeBankBalance = bankAccount.getBalance();
        beforeTrustBalance = trustAccount.getBalance();
        int amountReq = 10000;

        int resultBank = beforeBankBalance - amountReq;
        int resultTrust = beforeTrustBalance + amountReq;

        OpenBankingWithdrawRequest openBankingWithdrawRequest = new OpenBankingWithdrawRequest(
            payMember.getId(), null, fintechUseNum, trustAccount.getAccountNumber(), accountHolder,
            bankAccount.getAccountNumber(), amountReq);

        openBankingFacadeService.withdraw(openBankingWithdrawRequest);

        assertThat(bankAccount.getBalance()).isEqualTo(resultBank);
        assertThat(trustAccount.getBalance()).isEqualTo(resultTrust);
      }
    }

    @Nested
    @DisplayName("??????")
    class whenFail {

      @Nested
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @TestInstance(Lifecycle.PER_CLASS)
      @DisplayName("??????????????? ????????? ?????????")
      class BankAccountZeroBalance {

        BankAccount zeroBankAccount;

        @BeforeEach
        void setUpZeroBankAccount() {
          zeroBankAccount = new BankAccount("11374623", "????????????", 0, payMember, "?????????");
          bankAccountRepository.save(zeroBankAccount);
        }

        @Order(1)
        @Test
        @DisplayName("InsufficientBankAccountException??? ????????????")
        void insufficientBankAccountException() {
          beforeTrustBalance = trustAccount.getBalance();

          OpenBankingWithdrawRequest openBankingWithdrawRequest = new OpenBankingWithdrawRequest(
              payMember.getId(), null, fintechUseNum, trustAccount.getAccountNumber(),
              accountHolder, zeroBankAccount.getAccountNumber(), 10000);

          assertThrows(InsufficientBankAccountException.class,
              () -> openBankingFacadeService.withdraw(openBankingWithdrawRequest));
        }

        @Order(2)
        @Test
        @DisplayName("?????? ????????? ????????? ????????? ??????.")
        void notChangeBalance() {
          assertThat(zeroBankAccount.getBalance()).isZero();
          assertThat(trustAccount.getBalance()).isEqualTo(beforeTrustBalance);
        }
      }

      @Nested
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @TestInstance(Lifecycle.PER_CLASS)
      @DisplayName("???????????? ????????? inactive??? ??????")
      class BankAccountInactive {

        BankAccount inactiveBankAccount;

        @BeforeEach
        void setUpZeroBankAccount() {
          inactiveBankAccount = new BankAccount("11239847", "????????????", 1000, payMember, "?????????",
              StatusType.INACTIVE);
          bankAccountRepository.save(inactiveBankAccount);
        }

        @Order(1)
        @Test
        @DisplayName("InactiveBankAccountExceptionException??? ????????????")
        void inactiveBankAccountExceptionException() {
          beforeBankBalance = inactiveBankAccount.getBalance();
          beforeTrustBalance = trustAccount.getBalance();

          OpenBankingWithdrawRequest openBankingWithdrawRequest = new OpenBankingWithdrawRequest(
              payMember.getId(), null, fintechUseNum, trustAccount.getAccountNumber(),
              accountHolder, inactiveBankAccount.getAccountNumber(), 10000);

          assertThrows(InactiveBankAccountException.class,
              () -> openBankingFacadeService.withdraw(openBankingWithdrawRequest));
        }

        @Order(2)
        @Test
        @DisplayName("?????? ????????? ????????? ????????? ??????.")
        void notChangeBalance() {
          assertThat(inactiveBankAccount.getBalance()).isEqualTo(beforeBankBalance);
          assertThat(trustAccount.getBalance()).isEqualTo(beforeTrustBalance);
        }
      }

      @Nested
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @TestInstance(Lifecycle.PER_CLASS)
      @DisplayName("??????????????? payMemberId??? ???????????? payMemberId??? ???????????? ????????????")
      class FailAuth {

        @Order(1)
        @Test
        @DisplayName("BankAccountAuthenticationException??? ????????????")
        void bankAccountAuthenticationException() {
          Long payMemberIdReq = Long.MAX_VALUE;
          beforeBankBalance = bankAccount.getBalance();
          beforeTrustBalance = trustAccount.getBalance();

          OpenBankingWithdrawRequest openBankingWithdrawRequest = new OpenBankingWithdrawRequest(
              payMemberIdReq, null, fintechUseNum, trustAccount.getAccountNumber(), accountHolder,
              bankAccount.getAccountNumber(), 10000);

          assertThrows(BankAccountAuthenticationException.class,
              () -> openBankingFacadeService.withdraw(openBankingWithdrawRequest));
        }

        @Order(2)
        @Test
        @DisplayName("?????? ????????? ????????? ????????? ??????.")
        void notChangeBalance() {
          assertThat(bankAccount.getBalance()).isEqualTo(beforeBankBalance);
          assertThat(trustAccount.getBalance()).isEqualTo(beforeTrustBalance);
        }
      }

      @Nested
      @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
      @TestInstance(Lifecycle.PER_CLASS)
      @DisplayName("????????? ??????????????? ?????? ?????? ????????? inactive??? ??????")
      class InactiveTrustAccount {

        @Order(1)
        @Test
        @DisplayName("InactiveTrustAccountException??? ????????????")
        void inactiveTrustAccountException() {
          beforeBankBalance = bankAccount.getBalance();
          beforeTrustBalance = trustAccountInactive.getBalance();

          OpenBankingWithdrawRequest openBankingWithdrawRequest = new OpenBankingWithdrawRequest(
              payMember.getId(), null, fintechUseNum, trustAccountInactive.getAccountNumber(),
              accountHolder, bankAccount.getAccountNumber(), 10000);

          assertThrows(InactiveTrustAccountException.class,
              () -> openBankingFacadeService.withdraw(openBankingWithdrawRequest));
        }

        @Order(2)
        @Test
        @DisplayName("?????? ????????? ????????? ????????? ??????.")
        void notChangeBalance() {
          assertThat(bankAccount.getBalance()).isEqualTo(beforeBankBalance);
          assertThat(trustAccountInactive.getBalance()).isEqualTo(beforeTrustBalance);
        }
      }
    }
  }
}