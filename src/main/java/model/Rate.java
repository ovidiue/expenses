package model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Ovidiu on 15-May-18.
 */
@Entity
public class Rate {
    @Column
    private Double amount;
    @Column
    private Date date;
    @Column
    private String observations;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    public Rate(Double amount, Date date, String observations) {
        this.amount = amount;
        this.date = date;
        this.observations = observations;
    }

    public Double getAmount() {
        return amount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
