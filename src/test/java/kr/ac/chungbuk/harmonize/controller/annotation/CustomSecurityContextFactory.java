package kr.ac.chungbuk.harmonize.controller.annotation;

import kr.ac.chungbuk.harmonize.entity.User;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class CustomSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // User 객체 생성
        User user = User.builder()
                .loginId(customUser.username())
                .role(customUser.role())
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        );
        context.setAuthentication(auth);
        return context;
    }
}
