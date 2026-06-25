package com.rc.readcompass.jwt.service;

import com.rc.readcompass.jwt.dto.AuthDto;
import com.rc.readcompass.jwt.entity.CustomUserDetails;
import com.rc.readcompass.user.User;
import com.rc.readcompass.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 로그인 시 AuthenticationManager 가 사용자를 조회하는 곳.
 * LoginFilter 가 principal 자리에 email 을 넣어 보내므로 email 로 조회한다.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        AuthDto authDto = new AuthDto();
        authDto.setId(user.getId());
        authDto.setUsername(user.getNickname());
        authDto.setPassword(user.getPassword());
        authDto.setUserRole(user.getRole());

        return new CustomUserDetails(authDto);
    }
}
