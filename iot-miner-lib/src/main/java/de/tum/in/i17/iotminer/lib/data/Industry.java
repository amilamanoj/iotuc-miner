package de.tum.in.i17.iotminer.lib.data;


import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;

/**
 * Created by amilamanoj on 24.06.17.
 */
@Entity
public class Industry {

    @Id
    private Integer id;

    private String name;

    @OneToMany(cascade=ALL, mappedBy = "industry", fetch = FetchType.EAGER)
    @JsonBackReference
    private Set<UseCase> useCases;

    public Industry(Integer id, String name) {
        this.id = id;
        this.name = name;
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

    public Set<UseCase> getUseCases() {
        return useCases;
    }

    public void setUseCases(Set<UseCase> useCases) {
        this.useCases = useCases;
    }
}
