package com.example.yelloclient.classes;

import java.util.LinkedList;
import java.util.List;

public class PaymentTypes {
    public List<PaymentType> payment_types = new LinkedList<>();

    public PaymentType getCurrent() {
        for (PaymentType p: payment_types) {
            if (p.selected) {
                return p;
            }
        }
        return null;
    }
}
