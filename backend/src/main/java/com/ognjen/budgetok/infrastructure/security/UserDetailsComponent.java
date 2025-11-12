package com.ognjen.budgetok.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDetailsComponent implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

    var userBuilder = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
        .password(user.getPassword());

    if ("admin@example.com".equals(user.getEmail())) {
//      userBuilder.roles(Role.ADMIN.name(), Role.USER.name());
//    } else {
      userBuilder.roles(Role.USER.name());
    }

    return userBuilder.build();
  }
}
