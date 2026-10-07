package com.lankatech.spareparts.sales.strategy;

import com.lankatech.spareparts.sales.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CASH;
    }

    @Override
    public void processPayment(BigDecimal amount) {
        System.out.println("Cash payment processed: Rs. " + amount);
    }
}