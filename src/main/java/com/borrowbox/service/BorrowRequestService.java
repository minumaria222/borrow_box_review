package com.borrowbox.service;

import com.borrowbox.dto.BorrowRequestForm;
import com.borrowbox.dto.BorrowRequestResponse;
import com.borrowbox.model.BorrowRequest;
import com.borrowbox.model.Item;
import com.borrowbox.model.RequestStatus;
import com.borrowbox.model.Student;
import com.borrowbox.repository.BorrowRequestRepository;
import com.borrowbox.repository.ItemRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BorrowRequestService {
    private final BorrowRequestRepository requests;
    private final ItemRepository items;
    private final StudentService students;

    public BorrowRequestService(BorrowRequestRepository requests, ItemRepository items, StudentService students) {
        this.requests = requests;
        this.items = items;
        this.students = students;
    }

    @Transactional
    public BorrowRequestResponse create(BorrowRequestForm form) {
        Item item = lockItem(form.itemId());
        Student student = students.requireStudent(form.studentId());
        if (!item.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This item is not currently available.");
        }
        if (item.getOwner().getId().equals(student.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot borrow your own listing.");
        }
        validateDates(form.startDate(), form.expectedReturnDate(), item.getMaxBorrowDays());
        return BorrowRequestResponse.from(requests.save(new BorrowRequest(student, item, form.startDate(),
                form.expectedReturnDate(), form.message())));
    }

    @Transactional(readOnly = true)
    public List<BorrowRequestResponse> mine(Long studentId) {
        students.requireStudent(studentId);
        return requests.findByStudentIdOrderByRequestedAtDesc(studentId).stream().map(BorrowRequestResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<BorrowRequestResponse> received(Long ownerId) {
        students.requireStudent(ownerId);
        return requests.findByItemOwnerIdOrderByRequestedAtDesc(ownerId).stream().map(BorrowRequestResponse::from).toList();
    }

    @Transactional
    public BorrowRequestResponse approve(Long requestId, Long ownerId) {
        BorrowRequest request = requireRequest(requestId);
        Item item = lockItem(request.getItem().getId());
        requireOwner(item, ownerId);
        requireStatus(request, RequestStatus.PENDING);
        if (!item.isAvailable() || requests.existsByItemIdAndStatus(item.getId(), RequestStatus.APPROVED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Another request already has this item approved.");
        }
        item.setAvailable(false);
        request.setStatus(RequestStatus.APPROVED);
        return BorrowRequestResponse.from(request);
    }

    @Transactional
    public BorrowRequestResponse reject(Long requestId, Long ownerId) {
        BorrowRequest request = requireRequest(requestId);
        Item item = lockItem(request.getItem().getId());
        requireOwner(item, ownerId);
        requireStatus(request, RequestStatus.PENDING);
        request.setStatus(RequestStatus.REJECTED);
        return BorrowRequestResponse.from(request);
    }

    @Transactional
    public BorrowRequestResponse cancel(Long requestId, Long studentId) {
        BorrowRequest request = requireRequest(requestId);
        students.requireStudent(studentId);
        if (!request.getStudent().getId().equals(studentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the requesting student can cancel this request.");
        }
        requireStatus(request, RequestStatus.PENDING);
        request.setStatus(RequestStatus.CANCELLED);
        return BorrowRequestResponse.from(request);
    }

    @Transactional
    public BorrowRequestResponse markReturned(Long requestId, Long ownerId) {
        BorrowRequest request = requireRequest(requestId);
        Item item = lockItem(request.getItem().getId());
        requireOwner(item, ownerId);
        requireStatus(request, RequestStatus.APPROVED);
        request.setStatus(RequestStatus.RETURNED);
        item.setAvailable(true);
        return BorrowRequestResponse.from(request);
    }

    private BorrowRequest requireRequest(Long id) {
        return requests.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
    }

    private Item lockItem(Long id) {
        return items.findByIdForUpdate(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found."));
    }

    private void requireOwner(Item item, Long ownerId) {
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the item owner can manage this request.");
        }
    }

    private void requireStatus(BorrowRequest request, RequestStatus expected) {
        if (request.getStatus() != expected) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This request is no longer " + expected.name().toLowerCase() + ".");
        }
    }

    private void validateDates(LocalDate startDate, LocalDate returnDate, int maxDays) {
        if (startDate.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The start date cannot be in the past.");
        }
        if (!returnDate.isAfter(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The return date must be after the start date.");
        }
        if (ChronoUnit.DAYS.between(startDate, returnDate) > maxDays) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The borrowing period exceeds the item's maximum of " + maxDays + " days.");
        }
    }
}
