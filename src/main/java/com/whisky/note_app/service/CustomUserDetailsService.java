package com.whisky.note_app.service;

import com.whisky.note_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [CustomUserDetailsService]
 *
 * Spring Security의 UserDetailsService를 구현합니다.
 * JwtAuthenticationFilter가 토큰에서 꺼낸 이메일로 실제 User를 DB에서 조회할 때 사용합니다.
 *
 * [왜 필요한가?]
 * JwtAuthenticationFilter에서 토큰 검증 후 SecurityContext에 인증 객체를 넣으려면
 * 실제 UserDetails 객체가 필요합니다.
 * User 엔티티가 이미 UserDetails를 구현하고 있으므로 그대로 반환합니다.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }
}
