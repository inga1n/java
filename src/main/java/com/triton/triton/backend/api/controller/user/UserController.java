package com.triton.triton.backend.api.controller.user;

import java.util.List;
import java.util.Optional;

import com.triton.triton.backend.model.LocalUser;
import com.triton.triton.backend.model.repository.AddressRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest Controller for user data interactions.
 */
@RestController
@RequestMapping("/user")
public class UserController {

    /** The Address DAO. */
    private AddressRepository addressRepository;

    /**
     * Constructor for spring injection.
     * @param addressRepository
     */
    public UserController(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    /**
     * Gets all addresses for the given user and presents them.
     * @param user The authenticated user account.
     * @param userId The user ID to get the addresses of.
     * @return The list of addresses.
     */
    @GetMapping("/{userId}/address")
    public ResponseEntity<List<com.triton.triton.backend.model.Address>> getAddress(
            @AuthenticationPrincipal LocalUser user, @PathVariable Long userId) {
        if (!userHasPermission(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(addressRepository.findByUser_Id(userId));
    }

    /**
     * Allows the user to add a new address.
     * @param user The authenticated user.
     * @param userId The user id for the new address.
     * @param address The Address to be added.
     * @return The saved address.
     */
    @PutMapping("/{userId}/address")
    public ResponseEntity<com.triton.triton.backend.model.Address> putAddress(
            @AuthenticationPrincipal LocalUser user, @PathVariable Long userId,
            @RequestBody com.triton.triton.backend.model.Address address) {
        if (!userHasPermission(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        address.setId(null);
        LocalUser refUser = new LocalUser();
        refUser.setId(userId);
        address.setUser(refUser);
        return ResponseEntity.ok(addressRepository.save(address));
    }

    /**
     * Updates the given address.
     * @param user The authenticated user.
     * @param userId The user ID the address belongs to.
     * @param addressId The address ID to alter.
     * @param address The updated address object.
     * @return The saved address object.
     */
    @PatchMapping("/{userId}/address/{addressId}")
    public ResponseEntity<com.triton.triton.backend.model.Address> patchAddress(
            @AuthenticationPrincipal LocalUser user, @PathVariable Long userId,
            @PathVariable Long addressId, @RequestBody com.triton.triton.backend.model.Address address) {
        if (!userHasPermission(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (address.getId() == addressId) {
            Optional<com.triton.triton.backend.model.Address> opOriginalAddress = addressRepository.findById(addressId);
            if (opOriginalAddress.isPresent()) {
                LocalUser originalUser = opOriginalAddress.get().getUser();
                if (originalUser.getId() == userId) {
                    address.setUser(originalUser);
                    return ResponseEntity.ok(addressRepository.save(address));
                }
            }
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * Method to check if an authenticated user has permission to a user ID.
     * @param user The authenticated user.
     * @param id The user ID.
     * @return True if they have permission, false otherwise.
     */
    private boolean userHasPermission(LocalUser user, Long id) {
        return user.getId() == id;
    }

}
