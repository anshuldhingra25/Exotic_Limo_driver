package com.cabily.cabilydriver.Pojo;

/**
 * Created by user14 on 9/24/2015.
 */
public class PaymentdetailsPojo {
    private String pay_id;
    private String pay_duration_from;
    private String pay_duration_to;
    private String amount;
    private String pay_date;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    private String currency;


    public String getpay_id() {
        return pay_id;
    }

    public void setpay_id(String pay_id) {
        this.pay_id = pay_id;
    }

    public String getpay_duration_to() {
        return pay_duration_to;
    }

    public void setpay_duration_to(String pay_duration_to) {
        this.pay_duration_to = pay_duration_to;
    }

    public String getpay_duration_from() {
        return pay_duration_from;
    }

    public void setpay_duration_from(String pay_duration_from) {
        this.pay_duration_from = pay_duration_from;
    }

    public String getamount() {
        return amount;
    }

    public void setamount(String amount) {
        this.amount = amount;
    }

    public String getpay_date() {
        return pay_date;
    }

    public void setpay_date(String pay_date) {
        this.pay_date = pay_date;
    }
}
