package com.dangdang.server.domain.pay.daangnpay.domain.payMember.dto;

import com.dangdang.server.domain.pay.daangnpay.domain.connectionAccount.dto.GetConnectionAccountReceiveResponse;
import com.dangdang.server.domain.pay.daangnpay.domain.payMember.domain.FeeInfo;
import com.dangdang.server.domain.pay.kftc.common.dto.OpenBankingInquiryReceiveResponse;

public record ReceiveResponse(
    String receiveClientName,
    boolean isMyAccount,      // true일 경우 이후 프론트에서 출금 API를 호출해야 한다.
    String chargeAccountBankName,
    String chargeAccountNumber,
    int autoChargeAmount,
    int feeAmount,
    int freeMonthlyFeeCount) {

  public static ReceiveResponse of(
      OpenBankingInquiryReceiveResponse openBankingInquiryReceiveResponse,
      GetConnectionAccountReceiveResponse getConnectionAccountReceiveResponse, int autoChargeAmount,
      FeeInfo feeInfo) {
    return new ReceiveResponse(openBankingInquiryReceiveResponse.receiveClientName(),
        getConnectionAccountReceiveResponse.isMyAccount(),
        getConnectionAccountReceiveResponse.chargeAccountBankName(),
        getConnectionAccountReceiveResponse.chargeAccountNumber(), autoChargeAmount,
        feeInfo.getFeeAmount(), feeInfo.getFreeMonthlyFeeCount());
  }
}
