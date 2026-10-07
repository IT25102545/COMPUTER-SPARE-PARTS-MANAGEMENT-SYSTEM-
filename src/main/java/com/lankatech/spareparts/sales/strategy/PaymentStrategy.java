package com.lankatech.spareparts.sales.strategy;

import com.lankatech.spareparts.sales.entity.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentStrategy {

    PaymentMethod getPaymentMethod();

    void processPayment(BigDecimal amount);
}