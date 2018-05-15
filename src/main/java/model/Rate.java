package model;

import java.util.Date;

/**
 * Created by Ovidiu on 15-May-18.
 */
public class Rate {
    private Double amount;
    private Date date;
    private String observations;

    public Rate(Double amount, Date date, String observations) {
        this.amount = amount;
        this.date = date;
        this.observations = observations;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }
}
