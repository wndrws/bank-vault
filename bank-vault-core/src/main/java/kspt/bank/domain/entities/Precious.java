package kspt.bank.domain.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode
@Data
@NoArgsConstructor
@Entity
public class Precious {
    @Id
    @GeneratedValue
    private int id;

    int volume;

    @NotNull
    String name = "";

    public Precious(int volume, String name) {
        this.volume = volume;
        this.name = name;
    }
}
