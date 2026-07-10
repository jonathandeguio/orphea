package io.movetodata.library.models;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "template")
public class TemplateModel {

    // Declare the fields for each record - these are all the properties of a pet

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    public String name;
    public LocalDate dateOfBirth;
    public String color;

    @Transient
    public String age;

    public TemplateModel(Long id, String name, LocalDate dateOfBirth, String color, String age) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.color = color;
        this.age = age;
    }

    public TemplateModel() {

    }

    public TemplateModel(String name, LocalDate dateOfBirth, String color) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.color = color;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
