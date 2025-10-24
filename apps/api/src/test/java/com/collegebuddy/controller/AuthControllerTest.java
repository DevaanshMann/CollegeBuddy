package com.collegebuddy.controller;

import com.collegebuddy.domain.School;
import com.collegebuddy.domain.User;
import com.collegebuddy.repo.SchoolRepository;
import com.collegebuddy.repo.UserRepository;
import com.collegebuddy.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    MockMvc mvc;
    UserRepository users;
    SchoolRepository schools;
    PasswordEncoder encoder;
    JwtService jwt;

    @BeforeEach
    void setup() {
        users = mock(UserRepository.class);
        schools = mock(SchoolRepository.class);
        encoder = mock(PasswordEncoder.class);
        jwt = new JwtService();

        var ctrl = new AuthController(users, schools, encoder, jwt);
        // NOTE: no custom ControllerAdvice needed for ResponseStatusException in standalone setup
        mvc = MockMvcBuilders.standaloneSetup(ctrl).build();
    }

    @Test
    void signup_rejectsNonEdu() throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@gmail.com\",\"password\":\"p\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_conflictWhenEmailExists() throws Exception {
        when(users.existsByEmail("a@x.edu")).thenReturn(true);

        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@x.edu\",\"password\":\"p\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void signup_createsSchoolIfMissing_andUser() throws Exception {
        when(users.existsByEmail(anyString())).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("HASH");
        when(schools.findByDomain("x.edu")).thenReturn(java.util.Optional.empty());
        when(schools.save(any())).thenAnswer(i -> {
            School s = i.getArgument(0);
            s.setId(1L);
            return s;
        });
        when(users.save(any())).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(100L);
            return u;
        });

        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@x.edu\",\"password\":\"p\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        verify(schools).save(any());
        verify(users).save(any());
    }

    @Test
    void login_success_returnsToken() throws Exception {
        var u = new User();
        u.setId(5L);
        var s = new School();
        s.setId(2L);
        u.setSchool(s);
        u.setPasswordHash("HASH");

        when(users.findByEmail("a@x.edu")).thenReturn(java.util.Optional.of(u));
        when(encoder.matches("p", "HASH")).thenReturn(true);

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@x.edu\",\"password\":\"p\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void login_wrongPassword_unauthorized() throws Exception {
        var u = new User();
        u.setId(5L);
        var s = new School();
        s.setId(2L);
        u.setSchool(s);
        u.setPasswordHash("HASH");

        when(users.findByEmail("a@x.edu")).thenReturn(java.util.Optional.of(u));
        when(encoder.matches("bad", "HASH")).thenReturn(false);

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@x.edu\",\"password\":\"bad\"}"))
                .andExpect(status().isUnauthorized());
    }
}
