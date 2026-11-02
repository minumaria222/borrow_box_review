package com.borrowbox.repository;

import com.borrowbox.model.Item;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOrderByCreatedAtDesc();
    List<Item> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from Item item where item.id = :id")
    Optional<Item> findByIdForUpdate(@Param("id") Long id);
}
