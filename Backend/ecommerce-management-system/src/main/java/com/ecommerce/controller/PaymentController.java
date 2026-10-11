package com.ecommerce.controller;

import com.ecommerce.model.Payment;
import com.ecommerce.repository.PaymentRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }


    // ==========================================
    // PROCESS PAYMENT
    // ==========================================

    @PostMapping("/payment/process")
    public String processPayment(
            @RequestParam Integer orderId,
            @RequestParam String paymentMethod,
            HttpSession session) {


        // ------------------------------------------
        // CHECK LOGIN
        // ------------------------------------------

        Integer userId =
                (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }


        // ------------------------------------------
        // GET ORDER FROM CURRENT CHECKOUT SESSION
        // ------------------------------------------

        Integer sessionOrderId =
                (Integer) session.getAttribute(
                        "paymentOrderId"
                );


        if (sessionOrderId == null) {
            return "redirect:/cart";
        }


        // ------------------------------------------
        // MAKE SURE ORDER ID MATCHES
        // ------------------------------------------

        if (!sessionOrderId.equals(orderId)) {
            return "redirect:/payment";
        }


        // ------------------------------------------
        // CREATE PAYMENT
        // ------------------------------------------

        Payment payment =
                new Payment();


        payment.setOrderId(orderId);


        payment.setPaymentMethod(
                paymentMethod
        );


        payment.setPaymentStatus(
                "SUCCESS"
        );


        // ------------------------------------------
        // SAVE PAYMENT TO MYSQL
        // ------------------------------------------

        paymentRepository.save(payment);


        // ------------------------------------------
        // REMOVE TEMPORARY PAYMENT SESSION DATA
        // ------------------------------------------

        session.removeAttribute(
                "paymentOrderId"
        );

        session.removeAttribute(
                "paymentTotal"
        );


        // ------------------------------------------
        // PAYMENT SUCCESS
        // ------------------------------------------

        return "redirect:/order-success";
    }

}