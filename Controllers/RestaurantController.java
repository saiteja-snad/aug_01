package com.quickbite.controller;

import com.quickbite.dto.request.RestaurantRequest;
import com.quickbite.dto.response.RestaurantResponse;
import com.quickbite.service.RestaurantService;
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
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurants", description = "Restaurant management")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new restaurant (restaurant owners only)")
    public ResponseEntity<RestaurantResponse> create(@Valid @RequestBody RestaurantRequest request,
                                                       Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.create(request, auth.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a restaurant owned by the caller")
    public ResponseEntity<RestaurantResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody RestaurantRequest request,
                                                       Authentication auth) {
        return ResponseEntity.ok(restaurantService.update(id, request, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Deactivate a restaurant owned by the caller")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        restaurantService.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant details with menu (public)")
    public ResponseEntity<RestaurantResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getById(id));
    }

    @GetMapping
    @Operation(summary = "List all active restaurants (public)")
    public ResponseEntity<List<RestaurantResponse>> getAll() {
        return ResponseEntity.ok(restaurantService.getAll());
    }

    @GetMapping("/search")
    @Operation(summary = "Search restaurants by name or cuisine (public)")
    public ResponseEntity<List<RestaurantResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(restaurantService.search(keyword));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List restaurants owned by the caller")
    public ResponseEntity<List<RestaurantResponse>> getMine(Authentication auth) {
        return ResponseEntity.ok(restaurantService.getMyRestaurants(auth.getName()));
    }
}
