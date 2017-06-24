package de.tum.in.i17.iotminer.lib.data;


import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Set;

/**
 * Created by amilamanoj on 24.06.17.
 */
@Entity
public class Industry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    private String description;

    @ManyToMany(mappedBy = "industries")
    @JsonBackReference
    private Set<UseCase> useCases;

    public Industry() {
    }

    public Industry(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public Set<UseCase> getUseCases() {
        return useCases;
    }

    public void setUseCases(Set<UseCase> useCases) {
        this.useCases = useCases;
    }
}
