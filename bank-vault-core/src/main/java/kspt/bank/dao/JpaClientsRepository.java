package kspt.bank.dao;

import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaClientsRepository extends ClientsRepository, JpaRepository<Client, Integer> {
    @Override
    default void add(final Client client) {
        save(client);
    }

    @Override
    default Client find(final Integer id) {
        return findById(id).orElse(null);
    }

    @Override
    default boolean containsClientWith(final PassportInfo passportInfo) {
        final Client client = findByPassportInfo(passportInfo);
        return client != null;
    }

    @Override
    @Query("SELECT c FROM Client c WHERE c.passportInfo = :info")
    default Client getClientWith(final PassportInfo passportInfo) {
        return findByPassportInfo(passportInfo);
    }

    Client findByPassportInfo(final PassportInfo passportInfo);
}
