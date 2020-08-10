package com.wed18305.assignment1.web;

import com.wed18305.assignment1.model.User;
import com.wed18305.assignment1.services.User_Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class User_Controller{

    @Autowired
    private User_Service userService;

    // Create new user
    // POST ENDPOINT: http://localhost:8080/api/user 
    // JSON {"name":"neil",
    //       "username":"neil",
    //       "password":"1234",
    //       "contactNumber":"0425000000"} example format
    // UserType NOT CURRENTLY IMPLEMENTED 
    @PostMapping()
    public ResponseEntity<User> createNewUser(@RequestBody User user){
        //TODO check JSON formatting and that all parameters are supplied.
        User user1 = userService.saveOrUpdateUser(user);
        if(user1 == null){
            return new ResponseEntity<User>(user1, HttpStatus.CREATED);
        }else{
            return new ResponseEntity<User>(user1, HttpStatus.CREATED);
        }
    }

    // Get user via username and password
    // GET ENDPOINT: http://localhost:8080/api/user   
    // JSON{"name":"neil",
    //      "password":"1234"} example format
    @GetMapping()
    public ResponseEntity<Boolean> findByUser(@RequestBody User user){
        //TODO check JSON formatting and that name, password parameters are supplied.
        User user1 = userService.findByNameAndPassword(user.getName(), user.getPassword());
        if(user1 == null){
            return new ResponseEntity<Boolean>(false, HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<Boolean>(true, HttpStatus.FOUND);
        }
    }
}