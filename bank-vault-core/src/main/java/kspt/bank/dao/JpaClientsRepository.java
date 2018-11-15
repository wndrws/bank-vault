package kspt.bank.dao;

import com.google.common.base.Preconditions;
import kspt.bank.boundaries.ClientsRepository;
import kspt.bank.domain.entities.Client;
import kspt.bank.domain.entities.PassportInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaClientsRepository extends ClientsRepository, JpaRepository<Client, Integer> {
    @Override
    default void add(final Client client) {
        Preconditions.checkArgument(client.getId() == null);
        save(client);
    }

    @Override
    default Client find(final Integer id) {
        return findById(id).orElse(null);
    }

    @Override
    default boolean containsClientWith(final PassportInfo passportInfo) {
        final Client client = getClientWith(passportInfo);
        return client != null;
    }

    @Override
    @Query("SELECT c FROM Client c WHERE c.passportInfo = :info")
    Client getClientWith(@Param("info") final PassportInfo passportInfo);
}
