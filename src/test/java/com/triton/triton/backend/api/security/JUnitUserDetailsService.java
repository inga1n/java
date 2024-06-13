package com.triton.triton.backend.api.security;

import java.util.Optional;

import com.triton.triton.backend.model.LocalUser;
import com.triton.triton.backend.model.dao.LocalUserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Сервис чтобы дать spring'у  Authentication Principals для юнит тестинга.
 */
@Service
@Primary
public class JUnitUserDetailsService implements UserDetailsService {

    /** The Local User DAO. */
    @Autowired
    private LocalUserDAO localUserDAO;

    /**
     * {@inheritDoc}
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<LocalUser> opUser = localUserDAO.findByUsernameIgnoreCase(username);
        if (opUser.isPresent())
            return opUser.get();
        return null;
    }

}
