package com.chronology.bot.repository;

import com.chronology.bot.model.UserContent;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepo extends CrudRepository<UserContent, UUID> {

    List<UserContent> findAllByUserId(long userId);

    @Query("SELECT uc FROM UserContent uc WHERE uc.userId in :usersIds and uc.date >= :fromEq and uc.date <= :toEq ORDER BY uc.date ASC")
    List<UserContent> findAllByUsersIdsAndDateBetweenAndOrderByDate(@Param("usersIds") Set<Long> usersIds, @Param("fromEq") LocalDate fromEq, @Param("toEq") LocalDate toEq);
}
