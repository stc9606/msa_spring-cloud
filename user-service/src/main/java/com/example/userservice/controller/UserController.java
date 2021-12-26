package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;

import com.example.userservice.vo.ResponseUser;
import io.micrometer.core.annotation.Timed;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.userservice.service.UserService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class UserController {

    private Environment env;
    private UserService userService;

    @Autowired
    private Greeting greeting;

    @Autowired
    public UserController(Environment env, UserService userService) {
        this.env = env;
        this.userService = userService;
    }

    @GetMapping("/heath_check")
    @Timed(value = "users.status", longTask = true)
    public String status() {
        return String.format("It's Working in User Service"
               +", port(local.server.port)=" + env.getProperty("local.server.port")
               +", port(server.port)=" + env.getProperty("server.port")
               +", token secret=" + env.getProperty("token.secret")
               +", token expiration time=" + env.getProperty("token.expiration_time"));
    }

    @GetMapping("/welcome")
    @Timed(value = "users.welcome", longTask = true)
    public String welcome() {
//        return env.getProperty("greeting.message");
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser requestUser) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserDto requestUserDto = mapper.map(requestUser, UserDto.class);

        UserDto responseUserDto = userService.createUser(requestUserDto);

        ResponseUser responseUser = mapper.map(responseUserDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUserByIdAll() {
        Iterable<UserEntity> responseEntity = userService.getUserByIdAll();

        List<ResponseUser> responseUsers = new ArrayList<>();
        responseEntity.forEach(userEntity -> {
            ResponseUser responseUser = new ModelMapper().map(userEntity, ResponseUser.class);

            responseUsers.add(responseUser);
        });

        return ResponseEntity.status(HttpStatus.OK).body(responseUsers);

    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> getUserByUserId(@PathVariable("userId") String userId) {
        UserDto responseUesrDto = userService.getUserByUserId(userId);

        ResponseUser responseUser = new ModelMapper().map(responseUesrDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }
}
