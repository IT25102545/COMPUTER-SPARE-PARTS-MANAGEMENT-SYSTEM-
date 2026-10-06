package com.lankatech.spareparts.sales.strategy;

import com.lankatech.spareparts.sales.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CardPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CARD;
    }

    @Override
    public void processPayment(BigDecimal amount) {
        System.out.println("Card payment processed: Rs. " + amount);
    }
}