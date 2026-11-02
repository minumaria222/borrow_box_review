package com.borrowbox.repository;

import com.borrowbox.model.BorrowRequest;
import com.borrowbox.model.RequestStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    List<BorrowRequest> findByStudentIdOrderByRequestedAtDesc(Long studentId);
    List<BorrowRequest> findByItemOwnerIdOrderByRequestedAtDesc(Long ownerId);
    boolean existsByItemId(Long itemId);
    boolean existsByItemIdAndStatus(Long itemId, RequestStatus status);
}
