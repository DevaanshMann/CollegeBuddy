package com.collegebuddy.controller;

import com.collegebuddy.domain.School;
import com.collegebuddy.domain.User;
import com.collegebuddy.repo.SchoolRepository;
import com.collegebuddy.repo.UserRepository;
import com.collegebuddy.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;

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
        mvc = MockMvcBuilders.standaloneSetup(ctrl).build();
    }

    // -------------------- SIGNUP --------------------

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
        when(schools.findByDomain("x.edu")).thenReturn(Optional.empty());
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
    void signup_usesExistingSchool_whenDomainAlreadyExists() throws Exception {
        var existing = new School();
        existing.setId(77L);
        existing.setDomain("cpp.edu");
        existing.setName("Cal Poly Pomona");

        when(users.existsByEmail("new@cpp.edu")).thenReturn(false);
        when(schools.findByDomain("cpp.edu")).thenReturn(Optional.of(existing));
        when(encoder.encode("pw")).thenReturn("H");

        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@cpp.edu\",\"password\":\"pw\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        verify(schools, never()).save(any());

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(users).save(cap.capture());
        User saved = cap.getValue();
        org.assertj.core.api.Assertions.assertThat(saved.getSchool()).isSameAs(existing);
    }

    @Test
    void signup_trimsAndLowercasesEmail_andSetsVerifiedTrue() throws Exception {
        when(users.existsByEmail("student@csun.edu")).thenReturn(false);
        when(schools.findByDomain("csun.edu")).thenReturn(Optional.of(new School()));
        when(encoder.encode("pw123")).thenReturn("HASH");

        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"  Student@CSUN.edu  \",\"password\":\"pw123\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(users).save(cap.capture());
        User saved = cap.getValue();

        org.assertj.core.api.Assertions.assertThat(saved.getEmail()).isEqualTo("student@csun.edu");
        org.assertj.core.api.Assertions.assertThat(saved.getPasswordHash()).isEqualTo("HASH");
        org.assertj.core.api.Assertions.assertThat(saved.isEmailVerified()).isTrue();
    }

    @Test
    void signup_badRequest_onNullBodyOrMissingFields() throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    Throwable ex = result.getResolvedException();
                    if (ex != null) {
                        boolean ok = (ex instanceof HttpMessageNotReadableException)
                                || (ex instanceof ResponseStatusException
                                && ex.getMessage() != null
                                && ex.getMessage().contains("email and password required"));
                        org.assertj.core.api.Assertions.assertThat(ok)
                                .as("expected HttpMessageNotReadableException OR ResponseStatusException('email and password required')")
                                .isTrue();
                    }
                });

        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_uppercaseEduDomain_isAccepted_andLowercased() throws Exception {
        when(users.existsByEmail("user@csun.edu")).thenReturn(false);

        School existing = new School();
        existing.setId(1L);
        existing.setDomain("csun.edu");
        existing.setName("CSUN");
        when(schools.findByDomain("csun.edu")).thenReturn(java.util.Optional.of(existing));

        when(encoder.encode("pw")).thenReturn("HASH");

        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@csun.EDU\",\"password\":\"pw\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getEmail())
                .isEqualTo("user@csun.edu");
    }


    @Test
    void signup_unsupportedMediaType_returns415() throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.TEXT_PLAIN)
                        .content("email=user@csun.edu&password=pw"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // -------------------- LOGIN --------------------

    @Test
    void login_success_returnsToken() throws Exception {
        var u = new User();
        u.setId(5L);
        var s = new School();
        s.setId(2L);
        u.setSchool(s);
        u.setPasswordHash("HASH");

        when(users.findByEmail("a@x.edu")).thenReturn(Optional.of(u));
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

        when(users.findByEmail("a@x.edu")).thenReturn(Optional.of(u));
        when(encoder.matches("bad", "HASH")).thenReturn(false);

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@x.edu\",\"password\":\"bad\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason(containsString("Invalid credentials")));
    }

    @Test
    void login_userNotFound_unauthorized() throws Exception {
        when(users.findByEmail("missing@edu.edu")).thenReturn(Optional.empty());

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"missing@edu.edu\",\"password\":\"pw\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason(containsString("Invalid credentials")));
    }

    @Test
    void login_badRequest_onNullBodyOrMissingFields() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    Throwable ex = result.getResolvedException();
                    if (ex != null) {
                        boolean ok = (ex instanceof HttpMessageNotReadableException)
                                || (ex instanceof ResponseStatusException
                                && ex.getMessage() != null
                                && ex.getMessage().contains("email and password required"));
                        org.assertj.core.api.Assertions.assertThat(ok).isTrue();
                    }
                });

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_whitespaceEmail_unauthorized() throws Exception {
        var u = new User();
        u.setId(10L);
        var s = new School();
        s.setId(3L);
        u.setSchool(s);
        u.setPasswordHash("HASH");
        when(users.findByEmail("user@cpp.edu")).thenReturn(java.util.Optional.of(u));
        when(encoder.matches("pw", "HASH")).thenReturn(true);

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"  user@cpp.edu  \",\"password\":\"pw\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_unsupportedMediaType_returns415() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.TEXT_PLAIN)
                        .content("email=user@cpp.edu&password=pw"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
