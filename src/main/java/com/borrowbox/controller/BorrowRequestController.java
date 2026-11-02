package com.borrowbox.controller;

import com.borrowbox.dto.BorrowRequestForm;
import com.borrowbox.dto.BorrowRequestResponse;
import com.borrowbox.service.BorrowRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class BorrowRequestController {
    private final BorrowRequestService requests;

    public BorrowRequestController(BorrowRequestService requests) { this.requests = requests; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowRequestResponse create(@Valid @RequestBody BorrowRequestForm form) { return requests.create(form); }

    @GetMapping("/mine")
    public List<BorrowRequestResponse> mine(@RequestParam Long studentId) { return requests.mine(studentId); }

    @GetMapping("/received")
    public List<BorrowRequestResponse> received(@RequestParam Long ownerId) { return requests.received(ownerId); }

    @PutMapping("/{id}/approve")
    public BorrowRequestResponse approve(@PathVariable Long id, @RequestParam Long ownerId) {
        return requests.approve(id, ownerId);
    }

    @PutMapping("/{id}/reject")
    public BorrowRequestResponse reject(@PathVariable Long id, @RequestParam Long ownerId) {
        return requests.reject(id, ownerId);
    }

    @PutMapping("/{id}/cancel")
    public BorrowRequestResponse cancel(@PathVariable Long id, @RequestParam Long studentId) {
        return requests.cancel(id, studentId);
    }

    @PutMapping("/{id}/return")
    public BorrowRequestResponse markReturned(@PathVariable Long id, @RequestParam Long ownerId) {
        return requests.markReturned(id, ownerId);
    }
}
