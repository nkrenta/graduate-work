package ru.skypro.homework;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ErrorHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String encodeAuth(String email) {
        return "Basic " + java.util.Base64.getEncoder()
                .encodeToString((email + ":password123").getBytes());
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/register")
                        .contentType("application/json")
                        .content("""
                                {
                                    "username": "%s",
                                    "password": "password123",
                                    "firstName": "Error",
                                    "lastName": "Handler",
                                    "phone": "+7(900)777-88-99",
                                    "role": "USER"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());
    }

    // ===== 404 Not Found =====

    @Test
    @DisplayName("GET /ads/99999 → 404 с JSON-телом ошибки")
    void notFound_returns404WithBody() throws Exception {
        register("notfounderr@example.com");
        String auth = encodeAuth("notfounderr@example.com");

        mockMvc.perform(get("/ads/99999")
                        .header("Authorization", auth))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ad not found: 99999"))
                .andExpect(jsonPath("$.path").value("/ads/99999"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    // ===== 403 Forbidden =====

    @Test
    @DisplayName("DELETE /ads/{id} чужого → 403 с JSON-телом ошибки")
    void forbidden_returns403WithBody() throws Exception {
        register("forbiddenowner@example.com");
        String ownerAuth = encodeAuth("forbiddenowner@example.com");
        org.springframework.mock.web.MockMultipartFile propsFile = new org.springframework.mock.web.MockMultipartFile(
                "properties", "", "application/json",
                """
                {"title": "Protected Ad", "price": 1000, "description": "This ad should be protected from others"}
                """.getBytes());
        org.springframework.mock.web.MockMultipartFile imageFile = new org.springframework.mock.web.MockMultipartFile(
                "image", "photo.jpg", "image/jpeg", new byte[]{0});
        var result = mockMvc.perform(multipart("/ads")
                        .file(propsFile)
                        .file(imageFile)
                        .header("Authorization", ownerAuth))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String adId = body.replaceAll(".*\"pk\":(\\d+).*", "$1");

        register("forbiddenattacker@example.com");
        String attackerAuth = encodeAuth("forbiddenattacker@example.com");

        mockMvc.perform(delete("/ads/" + adId)
                        .header("Authorization", attackerAuth))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access Denied"))
                .andExpect(jsonPath("$.path").value("/ads/" + adId));
    }

    // ===== 400 Bad Request (validation) =====

    @Test
    @DisplayName("PATCH /users/me/image с текстовым файлом → 400 с JSON-телом")
    void badRequest_invalidImage_returns400WithBody() throws Exception {
        register("badreq@example.com");
        String auth = encodeAuth("badreq@example.com");

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile(
                        "image", "file.txt", "text/plain", "not an image".getBytes());

        mockMvc.perform(multipart("/users/me/image")
                .file(file)
                .header("Authorization", auth)
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Only PNG, JPEG, GIF images are allowed"))
                .andExpect(jsonPath("$.path").value("/users/me/image"));
    }

    // ===== 401 Unauthorized =====

    @Test
    @DisplayName("GET /users/me без авторизации → 401")
    void unauthorized_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ===== 400 incorrect password =====

    @Test
    @DisplayName("POST /users/set_password с неверным текущим → 400")
    void badRequest_wrongPassword_returns400() throws Exception {
        register("wrongpaserr@example.com");
        String auth = encodeAuth("wrongpaserr@example.com");

        mockMvc.perform(post("/users/set_password")
                        .header("Authorization", auth)
                        .contentType("application/json")
                        .content("""
                                {"currentPassword": "wrongcurrent123", "newPassword": "newpassword123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    // ===== Image serving =====

    @Test
    @DisplayName("GET /images/users/nonexistent.png → 404")
    void getImage_nonExistent_returns404() throws Exception {
        mockMvc.perform(get("/images/users/nonexistent.png"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /images/ads/nonexistent.jpg → 404")
    void getImage_adsNonExistent_returns404() throws Exception {
        mockMvc.perform(get("/images/ads/nonexistent.jpg"))
                .andExpect(status().isNotFound());
    }
}
