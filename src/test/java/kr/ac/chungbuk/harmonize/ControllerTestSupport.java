package kr.ac.chungbuk.harmonize;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.chungbuk.harmonize.config.SecurityConfig;
import kr.ac.chungbuk.harmonize.controller.MusicController;
import kr.ac.chungbuk.harmonize.repository.UserRepository;
import kr.ac.chungbuk.harmonize.security.JwtAuthenticationEntryPoint;
import kr.ac.chungbuk.harmonize.security.JwtAuthenticationFilter;
import kr.ac.chungbuk.harmonize.service.LogService;
import kr.ac.chungbuk.harmonize.service.MusicActionService;
import kr.ac.chungbuk.harmonize.service.MusicService;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;

@WebMvcTest(controllers = {
        MusicController.class
})
@Import(SecurityConfig.class)
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected UserRepository userRepository;

    @MockBean
    protected MusicService musicService;

    @MockBean
    protected MusicActionService musicActionService;

    @MockBean
    protected LogService logService;

    @MockBean
    protected FileHandler fileHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
}
