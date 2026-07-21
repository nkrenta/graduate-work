package ru.skypro.homework;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String REGISTER_BODY = """
            {
                "username": "auth_test@example.com",
                "password": "password123",
                "firstName": "Auth",
                "lastName": "Tester",
                "phone": "+7(900)111-22-33",
                "role": "USER"
            }
            """;

    @Test
    @DisplayName("POST /register — регистрация нового пользователя → 201")
    void register_newUser_returns201() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_BODY))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /register — дубликат email → 400")
    void register_duplicateEmail_returns400() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_BODY))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_BODY))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /login — корректный пароль → 200")
    void login_correctPassword_returns200() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_BODY))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "auth_test@example.com", "password": "password123"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /login — неверный пароль → 401")
    void login_wrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_BODY))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "auth_test@example.com", "password": "wrongpassword"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /login — несуществующий пользователь → 401")
    void login_nonExistentUser_returns401() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "ghost@example.com", "password": "password123"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
