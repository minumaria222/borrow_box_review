package com.borrowbox.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.borrowbox.dto.BorrowRequestForm;
import com.borrowbox.model.BorrowRequest;
import com.borrowbox.model.Item;
import com.borrowbox.model.RequestStatus;
import com.borrowbox.model.Student;
import com.borrowbox.repository.BorrowRequestRepository;
import com.borrowbox.repository.ItemRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BorrowRequestServiceTest {
    @Mock
    private BorrowRequestRepository requests;
    @Mock
    private ItemRepository items;
    @Mock
    private StudentService students;

    private BorrowRequestService service;
    private Student asha;
    private Student rahul;
    private Item calculator;

    @BeforeEach
    void setUp() {
        service = new BorrowRequestService(requests, items, students);
        asha = new Student("Asha", "CSE", "asha@borrowbox.demo");
        rahul = new Student("Rahul", "ECE", "rahul@borrowbox.demo");
        ReflectionTestUtils.setField(asha, "id", 1L);
        ReflectionTestUtils.setField(rahul, "id", 2L);
        calculator = new Item("Scientific Calculator", "Electronics", "Test item", "Good", "ECE block", 7, null, rahul);
        ReflectionTestUtils.setField(calculator, "id", 20L);
    }

    @Test
    void rejectsBorrowingOwnItem() {
        calculator.setOwner(asha);
        when(items.findByIdForUpdate(20L)).thenReturn(Optional.of(calculator));
        when(students.requireStudent(1L)).thenReturn(asha);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.create(new BorrowRequestForm(1L, 20L, LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(2), null)));

        assertEquals(403, exception.getStatusCode().value());
        verify(requests, never()).save(any());
    }

    @Test
    void rejectsBorrowingPeriodBeyondListingLimit() {
        when(items.findByIdForUpdate(20L)).thenReturn(Optional.of(calculator));
        when(students.requireStudent(1L)).thenReturn(asha);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.create(new BorrowRequestForm(1L, 20L, LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(9), null)));

        assertEquals(400, exception.getStatusCode().value());
        verify(requests, never()).save(any());
    }

    @Test
    void approvalReservesItemAndReturnReleasesIt() {
        BorrowRequest request = pendingRequest();
        when(requests.findById(5L)).thenReturn(Optional.of(request));
        when(items.findByIdForUpdate(20L)).thenReturn(Optional.of(calculator));
        when(requests.existsByItemIdAndStatus(20L, RequestStatus.APPROVED)).thenReturn(false);

        service.approve(5L, 2L);

        assertEquals(RequestStatus.APPROVED, request.getStatus());
        assertFalse(calculator.isAvailable());

        when(requests.findById(5L)).thenReturn(Optional.of(request));
        service.markReturned(5L, 2L);

        assertEquals(RequestStatus.RETURNED, request.getStatus());
        assertTrue(calculator.isAvailable());
    }

    @Test
    void rejectsSecondActiveApprovalForSameItem() {
        BorrowRequest request = pendingRequest();
        when(requests.findById(5L)).thenReturn(Optional.of(request));
        when(items.findByIdForUpdate(20L)).thenReturn(Optional.of(calculator));
        when(requests.existsByItemIdAndStatus(20L, RequestStatus.APPROVED)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.approve(5L, 2L));

        assertEquals(409, exception.getStatusCode().value());
        assertEquals(RequestStatus.PENDING, request.getStatus());
    }

    @Test
    void onlyOwnerCanApproveRequest() {
        BorrowRequest request = pendingRequest();
        when(requests.findById(5L)).thenReturn(Optional.of(request));
        when(items.findByIdForUpdate(20L)).thenReturn(Optional.of(calculator));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.approve(5L, 1L));

        assertEquals(403, exception.getStatusCode().value());
        assertEquals(RequestStatus.PENDING, request.getStatus());
    }

    private BorrowRequest pendingRequest() {
        return new BorrowRequest(asha, calculator, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), null);
    }
}
