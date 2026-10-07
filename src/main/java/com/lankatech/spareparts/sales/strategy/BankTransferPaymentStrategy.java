package com.lankatech.spareparts.sales.strategy;

import com.lankatech.spareparts.sales.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BankTransferPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.BANK_TRANSFER;
    }

    @Override
    public void processPayment(BigDecimal amount) {
        System.out.println("Bank transfer payment processed: Rs. " + amount);
    }
}