package model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Ovidiu on 15-May-18.
 */
@Entity
public class Expense {
    @Column
    public String title;
    @Column
    public String description;
    @Column
    public boolean recurrent;
    @Column
    public Date createdOn;
    @Column
    public Date dueDate;
    @Column
    public Double amount;
   /* @Column*/
    public Category category;
    @Column
    public ArrayList<Tag> tags;
    @Column
    public ArrayList<Rate> payedRates;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    public Expense(String title,
                   String description,
                   boolean recurrent,
                   Date dueDate,
                   Double amount,
                   Category category) {
        this.title = title;
        this.description = description;
        this.recurrent = recurrent;
        this.createdOn = new Date();
        this.dueDate = dueDate;
        this.amount = amount;
        this.category = category;
        this.tags = new ArrayList<>();
        this.payedRates = new ArrayList<>();
    }

    public Expense(String title,
                   String description,
                   boolean recurrent,
                   Date dueDate,
                   Double amount) {
        this.title = title;
        this.description = description;
        this.recurrent = recurrent;
        this.createdOn = new Date();
        this.dueDate = dueDate;
        this.amount = amount;
        this.tags = new ArrayList<>();
        this.payedRates = new ArrayList<>();
    }

    public Expense() {
        this.createdOn = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRecurrent() {
        return recurrent;
    }

    public void setRecurrent(boolean recurrent) {
        this.recurrent = recurrent;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }

    public void setTags(ArrayList<Tag> tags) {
        this.tags = tags;
    }

    public ArrayList<Rate> getPayedRates() {
        return payedRates;
    }

    public void setPayedRates(ArrayList<Rate> payedRates) {
        this.payedRates = payedRates;
    }
}
