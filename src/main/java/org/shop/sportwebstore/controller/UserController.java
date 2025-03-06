package org.shop.sportwebstore.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.model.dto.UserDto;
import org.shop.sportwebstore.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getUser(){
        return userService.finAllUsers();
    }

    @GetMapping("/customers")
    public ResponseEntity<?> getCustomers(){
        return userService.finAllCustomers();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto user){
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }
}
