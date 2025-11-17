package com.example.activity_diary.security;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(u.getEmail()) // Important!
                .password(u.getPassword())
                .disabled(!u.getEnabled())
                .roles("USER")
                .build();
    }
}


