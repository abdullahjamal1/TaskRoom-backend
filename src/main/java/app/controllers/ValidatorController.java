package app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.repositories.UserRepository;

@RestController
@RequestMapping("/validate")
public class ValidatorController {
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/username")
    public boolean isUserNameUnique(@RequestParam("username") String username){
        if(userRepository.findOneByEmail(username) != null)
            return false;
        else return true;
    }
    
}
