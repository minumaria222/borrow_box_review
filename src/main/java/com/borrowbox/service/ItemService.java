package com.borrowbox.service;

import com.borrowbox.dto.ItemForm;
import com.borrowbox.dto.ItemResponse;
import com.borrowbox.model.Item;
import com.borrowbox.model.Student;
import com.borrowbox.repository.BorrowRequestRepository;
import com.borrowbox.repository.ItemRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ItemService {
    private final ItemRepository items;
    private final StudentService students;
    private final BorrowRequestRepository requests;

    public ItemService(ItemRepository items, StudentService students, BorrowRequestRepository requests) {
        this.items = items;
        this.students = students;
        this.requests = requests;
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> list() {
        return items.findAllByOrderByCreatedAtDesc().stream().map(ItemResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ItemResponse get(Long id) { return ItemResponse.from(requireItem(id)); }

    @Transactional(readOnly = true)
    public List<ItemResponse> listOwnedBy(Long ownerId) {
        return items.findByOwnerIdOrderByCreatedAtDesc(ownerId).stream().map(ItemResponse::from).toList();
    }

    @Transactional
    public ItemResponse create(ItemForm form) {
        Student owner = students.requireStudent(form.ownerId());
        return ItemResponse.from(items.save(new Item(form.name(), form.category(), form.description(),
                form.conditionLabel(), form.location(), form.maxBorrowDays(), form.imageUrl(), owner)));
    }

    @Transactional
    public ItemResponse update(Long id, Long ownerId, ItemForm form) {
        Item item = requireItem(id);
        requireOwner(item, ownerId);
        if (!item.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An item cannot be edited while it is borrowed.");
        }
        if (!ownerId.equals(form.ownerId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The listing owner cannot be changed.");
        }
        apply(item, form);
        return ItemResponse.from(item);
    }

    @Transactional
    public void delete(Long id, Long ownerId) {
        Item item = requireItem(id);
        requireOwner(item, ownerId);
        if (!item.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Return the item before deleting this listing.");
        }
        if (requests.existsByItemId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A listing with request history cannot be deleted.");
        }
        items.delete(item);
    }

    public Item requireItem(Long id) {
        return items.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found."));
    }

    public void requireOwner(Item item, Long ownerId) {
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the listing owner can do that.");
        }
    }

    private void apply(Item item, ItemForm form) {
        item.setName(form.name());
        item.setCategory(form.category());
        item.setDescription(form.description());
        item.setConditionLabel(form.conditionLabel());
        item.setLocation(form.location());
        item.setMaxBorrowDays(form.maxBorrowDays());
        item.setImageUrl(form.imageUrl());
    }
}
