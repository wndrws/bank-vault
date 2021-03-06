package kspt.bank.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class PassportInfo {
    @NotNull
    @Column(unique = true)
    String serial;

    @NotNull
    String firstName;

    @NotNull
    String lastName;

    String patronymic;

    LocalDate birthDate;
}