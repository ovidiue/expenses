package model;

import javafx.scene.paint.Color;

import javax.persistence.*;

/**
 * Created by Ovidiu on 15-May-18.
 */
@Entity
public class Category {
    private final String DEFAULT_COLOR = Color.WHITE.toString();
    @Column
    private String name;
    @Column(columnDefinition = "clob")
    @Lob
    private String description;
    @Column
    private String color;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    public Category() {
    }

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.color = DEFAULT_COLOR;
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

    @Override
    public String toString() {
        return this.name;
    }
}
