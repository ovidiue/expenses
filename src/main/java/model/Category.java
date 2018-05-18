package model;

import javax.persistence.*;

/**
 * Created by Ovidiu on 15-May-18.
 */
@Entity
public class Category {
    @Column
    public String name;
    @Column
    public String description;
    @Column
    public String color;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Category(String name, String description, String color) {
        this.name = name;
        this.description = description;
        this.color = color;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
