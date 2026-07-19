package ru.skypro.homework;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String encodeAuth(String email) {
        return "Basic " + java.util.Base64.getEncoder()
                .encodeToString((email + ":password123").getBytes());
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "%s",
                                    "password": "password123",
                                    "firstName": "Test",
                                    "lastName": "User",
                                    "phone": "+7(900)555-66-77",
                                    "role": "USER"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());
    }

    // ===== GET /users/me =====

    @Test
    @DisplayName("GET /users/me — получение профиля → 200 + все поля User DTO")
    void getUser_withAuth_returns200() throws Exception {
        register("getme@example.com");
        String auth = encodeAuth("getme@example.com");

        mockMvc.perform(get("/users/me")
                        .header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("getme@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.phone").value("+7(900)555-66-77"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("GET /users/me — без авторизации → 401")
    void getUser_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ===== PATCH /users/me =====

    @Test
    @DisplayName("PATCH /users/me — обновление данных → 200 + UpdateUser")
    void updateUser_withAuth_returns200() throws Exception {
        register("updateme@example.com");
        String auth = encodeAuth("updateme@example.com");

        mockMvc.perform(patch("/users/me")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Updated", "lastName": "Name", "phone": "+7(999)000-11-22"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.phone").value("+7(999)000-11-22"));
    }

    @Test
    @DisplayName("PATCH /users/me — проверка что данные сохранились в БД")
    void updateUser_verifyPersisted() throws Exception {
        register("persist@example.com");
        String auth = encodeAuth("persist@example.com");

        mockMvc.perform(patch("/users/me")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Persisted", "lastName": "Data", "phone": "+7(111)222-33-44"}
                                """))
                .andExpect(status().isOk());

        // Проверяем что данные сохранились
        mockMvc.perform(get("/users/me")
                        .header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Persisted"))
                .andExpect(jsonPath("$.lastName").value("Data"))
                .andExpect(jsonPath("$.phone").value("+7(111)222-33-44"));
    }

    // ===== POST /users/set_password =====

    @Test
    @DisplayName("POST /users/set_password — корректный текущий пароль → 200")
    void setPassword_correctCurrent_returns200() throws Exception {
        register("setpass@example.com");
        String auth = encodeAuth("setpass@example.com");

        mockMvc.perform(post("/users/set_password")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "password123", "newPassword": "newpassword123"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /users/set_password — неверный текущий пароль → 400")
    void setPassword_wrongCurrent_returns400() throws Exception {
        register("wrongpass@example.com");
        String auth = encodeAuth("wrongpass@example.com");

        mockMvc.perform(post("/users/set_password")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "wrongcurrent123", "newPassword": "newpassword123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    @DisplayName("POST /users/set_password — после смены пароля старый не работает")
    void setPassword_afterChange_oldPasswordFails() throws Exception {
        register("changepass@example.com");
        String oldAuth = encodeAuth("changepass@example.com");

        // Меняем пароль
        mockMvc.perform(post("/users/set_password")
                        .header("Authorization", oldAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "password123", "newPassword": "newpassword123"}
                                """))
                .andExpect(status().isOk());

        // Старый пароль не работает
        mockMvc.perform(get("/users/me")
                        .header("Authorization", oldAuth))
                .andExpect(status().isUnauthorized());

        // Новый пароль работает
        String newAuth = encodeAuth("changepass@example.com").replace(
                java.util.Base64.getEncoder().encodeToString("changepass@example.com:password123".getBytes()),
                java.util.Base64.getEncoder().encodeToString("changepass@example.com:newpassword123".getBytes())
        );
        mockMvc.perform(get("/users/me")
                        .header("Authorization", newAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("changepass@example.com"));
    }

    // ===== PATCH /users/me/image =====

    @Test
    @DisplayName("PATCH /users/me/image — загрузка валидного PNG → 200")
    void updateUserImage_validPng_returns200() throws Exception {
        register("avatar@example.com");
        String auth = encodeAuth("avatar@example.com");

        MockMultipartFile image = new MockMultipartFile(
                "image", "avatar.png", "image/png", new byte[]{0});

        mockMvc.perform(multipart("/users/me/image")
                        .file(image)
                        .header("Authorization", auth)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image").value(org.hamcrest.Matchers.startsWith("/images/users/")));
    }

    @Test
    @DisplayName("PATCH /users/me/image — текстовый файл → 400")
    void updateUserImage_textFile_returns400() throws Exception {
        register("avatarval@example.com");
        String auth = encodeAuth("avatarval@example.com");

        MockMultipartFile fakeFile = new MockMultipartFile(
                "image", "file.txt", "text/plain", "not an image".getBytes());

        mockMvc.perform(multipart("/users/me/image")
                .file(fakeFile)
                .header("Authorization", auth)
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only PNG, JPEG, GIF images are allowed"));
    }

    @Test
    @DisplayName("PATCH /users/me/image — файл >5MB → 400")
    void updateUserImage_tooLarge_returns400() throws Exception {
        register("avatarbig@example.com");
        String auth = encodeAuth("avatarbig@example.com");

        byte[] bigData = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile bigFile = new MockMultipartFile(
                "image", "big.png", "image/png", bigData);

        mockMvc.perform(multipart("/users/me/image")
                .file(bigFile)
                .header("Authorization", auth)
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /users/me/image — без авторизации → 401")
    void updateUserImage_withoutAuth_returns401() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "avatar.png", "image/png", new byte[]{0});

        mockMvc.perform(multipart("/users/me/image")
                .file(image)
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
                .andExpect(status().isUnauthorized());
    }
}
