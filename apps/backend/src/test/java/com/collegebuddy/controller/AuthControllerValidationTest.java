package com.collegebuddy.controller;

import com.collegebuddy.domain.School;
import com.collegebuddy.domain.User;
import com.collegebuddy.repo.SchoolRepository;
import com.collegebuddy.repo.UserRepository;
import com.collegebuddy.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
class AuthControllerValidationTest {

    MockMvc mvc;
    UserRepository users;
    SchoolRepository schools;
    PasswordEncoder encoder;

    @BeforeEach
    void setup() {
        users   = Mockito.mock(UserRepository.class);
        schools = Mockito.mock(SchoolRepository.class);
        encoder = Mockito.mock(PasswordEncoder.class);
        var jwt = new JwtService();

        var ctrl = new AuthController(users, schools, encoder, jwt);
        mvc = MockMvcBuilders
                .standaloneSetup(ctrl)
                .setControllerAdvice(new com.collegebuddy.globalexc.GlobalExceptionHandler())
                .build();
    }

    @Test void signup_missingBody_badRequest() throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test void signup_whitespaceEmail_badRequest() throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"   \",\"password\":\"p\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test void signup_whitespacePassword_badRequest() throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@x.edu\",\"password\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test void signup_invalidEmail_noLocalPart_badRequest() throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"@x.edu\",\"password\":\"p\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test void signup_invalidEmail_noDomain_badRequest() throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@\",\"password\":\"p\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test void signup_invalidEmail_doubleAt_badRequest() throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@@x.edu\",\"password\":\"p\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test void signup_uppercaseEdu_normalized_ok_andLowercasedStored() throws Exception {
        when(users.existsByEmail("a@x.edu")).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("HASH");
        when(schools.findByDomain("x.edu")).thenReturn(java.util.Optional.empty());
        when(schools.save(any())).thenAnswer(i -> { School s=i.getArgument(0); s.setId(1L); return s; });
        when(users.save(any())).thenAnswer(i -> { User u=i.getArgument(0); u.setId(100L); return u; });

        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"A@X.EDU\",\"password\":\"p\"}"))
                .andExpect(status().isOk());

        verify(users).existsByEmail("a@x.edu");
    }

    @Test void signup_overlongFields_badRequest() throws Exception {
        String long256 = "a".repeat(256);
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\""+ long256 +"@x.edu\",\"password\":\"p\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test void login_missingBody_badRequest() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
