package com.lankatech.spareparts.sales.strategy;

import com.lankatech.spareparts.sales.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentContext {

    private final Map<PaymentMethod, PaymentStrategy> strategies =
            new EnumMap<>(PaymentMethod.class);

    public PaymentContext(List<PaymentStrategy> paymentStrategies) {

        for (PaymentStrategy strategy : paymentStrategies) {
            strategies.put(strategy.getPaymentMethod(), strategy);
        }
    }

    public void processPayment(PaymentMethod paymentMethod, BigDecimal amount) {

        PaymentStrategy strategy = strategies.get(paymentMethod);

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Unsupported payment method: " + paymentMethod
            );
        }

        strategy.processPayment(amount);
    }
}