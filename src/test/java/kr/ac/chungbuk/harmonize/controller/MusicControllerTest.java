package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.ControllerTestSupport;
import kr.ac.chungbuk.harmonize.controller.annotation.WithMockCustomUser;
import kr.ac.chungbuk.harmonize.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MusicControllerTest extends ControllerTestSupport {

    @DisplayName("새로운 음악을 생성한다.")
    @WithMockCustomUser(role = Role.ADMIN)
    @Test
    void createMusic() throws Exception {
        // given
        MockHttpServletRequestBuilder request = createMusicCreateRequest("BALLADE", "123456");

        // when then
        mockMvc.perform(request)
//                .andDo(print())
                .andExpect(status().isCreated());
    }

    @DisplayName("회원 권한으로 음악을 생성하면 403 오류가 응답된다.")
    @WithMockCustomUser(role = Role.USER)
    @Test
    void createMusicRoleUser() throws Exception {
        // given
        MockHttpServletRequestBuilder request = createMusicCreateRequest("BALLADE", "123456");

        // when then
        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @DisplayName("음악 생성시 장르 문자열 검증 오류시 400 오류가 응답된다.")
    @WithMockCustomUser(role = Role.ADMIN)
    @Test
    void createMusicValidateGenre() throws Exception {
        // given
        MockHttpServletRequestBuilder request = createMusicCreateRequest("발라드", "123456");

        // when then
        mockMvc.perform(request)
//                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("genre"));
    }

    @DisplayName("음악 생성시 노래방 번호 길이가 50을 넘으면 400 오류가 응답된다.")
    @WithMockCustomUser(role = Role.ADMIN)
    @Test
    void createMusicValidateKaraokeNum() throws Exception {
        // given
        StringBuilder numBuilder = new StringBuilder();
        for (int i = 0; i < 51; i++) {
            numBuilder.append("1");
        }

        MockHttpServletRequestBuilder request = createMusicCreateRequest("발라드", numBuilder.toString());

        // when then
        mockMvc.perform(request)
//                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("karaokeNum"));
    }


    private static MockHttpServletRequestBuilder createMusicCreateRequest(String genre, String karaokeNum) {
        return multipart("/api/music")
                .param("title", "제목")
                .param("genre", genre)
                .param("karaokeNum", karaokeNum)
                .param("releaseDate", "2010-01-01T00:00:00")
                .param("playLink", "link.com")
                .param("groupId", "1")
                .param("themes", "테마1", "테마2") // 여러 값 전달
                .contentType(MediaType.MULTIPART_FORM_DATA);
    }
}