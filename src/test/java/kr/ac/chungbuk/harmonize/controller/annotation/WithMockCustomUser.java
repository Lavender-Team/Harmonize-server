package kr.ac.chungbuk.harmonize.controller.annotation;


import kr.ac.chungbuk.harmonize.enums.Role;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = CustomSecurityContextFactory.class)
public @interface WithMockCustomUser {
    String username() default "admin";
    Role role() default Role.ADMIN;
}
