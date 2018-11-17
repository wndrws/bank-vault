package kspt.bank.domain.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
@EqualsAndHashCode
@Data
@NoArgsConstructor
public class Precious {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    int volume;

    @NotNull
    String name = "";

    public Precious(int volume, String name) {
        this.volume = volume;
        this.name = name;
    }
}
