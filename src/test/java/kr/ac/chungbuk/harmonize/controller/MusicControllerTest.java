package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.ControllerTestSupport;
import kr.ac.chungbuk.harmonize.controller.annotation.WithMockCustomUser;
import kr.ac.chungbuk.harmonize.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MusicControllerTest extends ControllerTestSupport {

    @DisplayName("새로운 음악을 생성한다.")
    @WithMockCustomUser(role = Role.ADMIN)
    @Test
    void createMusic() throws Exception {
        // given
        MockHttpServletRequestBuilder request = multipart("/api/music")
                .param("title", "제목")
                .param("genre", "BALLADE")
                .param("karaokeNum", "123456")
                .param("releaseDate", "2010-01-01T00:00:00")
                .param("playLink", "link.com")
                .param("groupId", "1")
                .param("themes", "테마1", "테마2") // 여러 값 전달
                .contentType(MediaType.MULTIPART_FORM_DATA);

        // when then
        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isCreated());
    }

}