package com.example.activity_diary.platform.web.security;

import com.example.activity_diary.entity.User;
import com.example.activity_diary.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityContextCurrentUserProviderTest {

    @Mock
    private UserRepository userRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserId_returnsLightUserIdWithoutRepositoryLookup() {
        LightUserDetails principal = new LightUserDetails(42L, "tester", "USER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        SecurityContextCurrentUserProvider provider = new SecurityContextCurrentUserProvider(userRepository);

        Long currentUserId = provider.getCurrentUserId();

        assertEquals(42L, currentUserId);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getCurrentUserId_fallsBackToUsernameLookupForGenericUserDetails() {
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername("tester")
                .password("secret")
                .authorities("ROLE_USER")
                .build();
        User user = User.builder()
                .username("tester")
                .build();
        user.setId(24L);

        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        SecurityContextCurrentUserProvider provider = new SecurityContextCurrentUserProvider(userRepository);

        Long currentUserId = provider.getCurrentUserId();

        assertEquals(24L, currentUserId);
        verify(userRepository).findByUsername("tester");
    }
}
