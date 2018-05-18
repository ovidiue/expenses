package model;

import javax.persistence.*;

/**
 * Created by Ovidiu on 15-May-18.
 */
@Entity
public class Tag {
    @Column
    private String name;
    @Column
    private String color;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    public Tag(String name) {
        this.name = name;
    }

    public Tag(String name, String color) {
        this.name = name;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
