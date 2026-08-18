package com.quickbite.controller;

import com.quickbite.dto.request.MenuItemRequest;
import com.quickbite.dto.response.MenuItemResponse;
import com.quickbite.service.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/menu-items")
@RequiredArgsConstructor
@Tag(name = "Menu", description = "Menu item management per restaurant")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add a menu item to a restaurant owned by the caller")
    public ResponseEntity<MenuItemResponse> addItem(@PathVariable Long restaurantId,
                                                      @Valid @RequestBody MenuItemRequest request,
                                                      Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuItemService.addItem(restaurantId, request, auth.getName()));
    }

    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a menu item")
    public ResponseEntity<MenuItemResponse> updateItem(@PathVariable Long restaurantId,
                                                         @PathVariable Long itemId,
                                                         @Valid @RequestBody MenuItemRequest request,
                                                         Authentication auth) {
        return ResponseEntity.ok(menuItemService.updateItem(restaurantId, itemId, request, auth.getName()));
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a menu item")
    public ResponseEntity<Void> deleteItem(@PathVariable Long restaurantId,
                                            @PathVariable Long itemId,
                                            Authentication auth) {
        menuItemService.deleteItem(restaurantId, itemId, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List the menu for a restaurant (public)")
    public ResponseEntity<List<MenuItemResponse>> getMenu(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuItemService.getMenu(restaurantId));
    }
}
