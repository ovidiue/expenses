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
    private String observation;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "expense_id")
    private Expense expense;
    @Id
    @GeneratedValue
    private int id;


    public Rate(Double amount, Date date, String observation) {
        this.amount = amount;
        this.date = date;
        this.observation = observation;
    }

    public Rate() {
    }

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    @Override
    public String toString() {
        return "Rate:" +
                "\namount:" + amount +
                "\ndate:" + date +
                "\nobservation;'" + observation;
    }

}
