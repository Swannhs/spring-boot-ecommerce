package com.swann.paymentservice;

import org.springframework.boot.SpringApplication;

public class TestPaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(PaymentServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
