//src/main/java/com/cloudflix/backend/security/services/UserDetailsServiceImpl.java
package com.cloudflix.backend.security.services;

import com.cloudflix.backend.entity.User;
import com.cloudflix.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary // Add this annotation
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    /*@Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));
        return UserDetailsImpl.build(user);
    }*/
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));

        if (!user.isActive()) { // <<< CHECK IF USER IS ACTIVE
            throw new UsernameNotFoundException("User account for " + email + " is deactivated.");
            // Or more specifically, a DisabledException or LockedException from Spring Security if you prefer
            // import org.springframework.security.authentication.DisabledException;
            // throw new DisabledException("User account is deactivated.");
        }

        return UserDetailsImpl.build(user);
    }
}