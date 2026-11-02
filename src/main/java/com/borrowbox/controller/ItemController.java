package com.borrowbox.controller;

import com.borrowbox.dto.ItemForm;
import com.borrowbox.dto.ItemResponse;
import com.borrowbox.service.ItemService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
public class ItemController {
    private final ItemService items;

    public ItemController(ItemService items) { this.items = items; }

    @GetMapping
    public List<ItemResponse> list(@RequestParam(required = false) Long ownerId) {
        return ownerId == null ? items.list() : items.listOwnedBy(ownerId);
    }

    @GetMapping("/{id}")
    public ItemResponse get(@PathVariable Long id) { return items.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse create(@Valid @RequestBody ItemForm form) { return items.create(form); }

    @PutMapping("/{id}")
    public ItemResponse update(@PathVariable Long id, @Valid @RequestBody ItemForm form) {
        return items.update(id, form.ownerId(), form);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @RequestParam Long ownerId) { items.delete(id, ownerId); }
}
