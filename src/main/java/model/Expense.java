package model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ovidiu on 15-May-18.
 */
@Entity
@Table(name = "expense")
public class Expense {
    @Column
    private String title;
    @Column(columnDefinition = "clob")
    @Lob
    private String description;
    @Column
    private boolean recurrent;
    @Column
    private Date createdOn;
    @Column
    private Date dueDate;
    @Column
    private Double amount;
    /*@Column*/
    @OneToOne(fetch = FetchType.EAGER)
    private Category category;
    /*@Column*/
    @ManyToMany(cascade = CascadeType.DETACH)
    private List<Tag> tags;
    @OneToMany(mappedBy = "expense",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Rate> payedRates;
    @Id
    @GeneratedValue
    private int id;

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

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Rate> getPayedRates() {
        return payedRates;
    }

    public void addRate(Rate rate) {
        this.payedRates.add(rate);
        rate.setExpense(this);
    }

    public void removeRate(Rate rate) {
        this.payedRates.remove(rate);
        rate.setExpense(null);
    }

    @Override
    public String toString() {
        return "Expense{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", recurrent=" + recurrent +
                ", createdOn=" + createdOn +
                ", dueDate=" + dueDate +
                ", amount=" + amount +
                ", category=" + category +
                ", tags=" + tags +
                ", payedRates=" + payedRates +
                ", id=" + id +
                '}';
    }
}
