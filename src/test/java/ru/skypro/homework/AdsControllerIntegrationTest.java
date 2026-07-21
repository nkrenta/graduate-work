package ru.skypro.homework;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private void registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "%s",
                                    "password": "password123",
                                    "firstName": "Test",
                                    "lastName": "User",
                                    "phone": "+7(900)000-00-00",
                                    "role": "USER"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());
    }

    private int createAd(String email, String auth) throws Exception {
        MockMultipartFile properties = new MockMultipartFile(
                "properties",
                "",
                "application/json",
                """
                {"title": "Test Ad Title", "price": 10000, "description": "This is a test ad description"}
                """.getBytes());

        InputStream imageStream = getClass().getResourceAsStream("/pictures_for_tests/monitor.jpg");
        MockMultipartFile image;
        if (imageStream != null) {
            image = new MockMultipartFile("image", "monitor.jpg", "image/jpeg", imageStream);
        } else {
            image = new MockMultipartFile("image", "monitor.jpg", "image/jpeg", new byte[]{0});
        }

        var result = mockMvc.perform(multipart("/ads")
                        .file(properties)
                        .file(image)
                        .header("Authorization", auth))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Ad Title"))
                .andExpect(jsonPath("$.price").value(10000))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String pk = responseBody.replaceAll(".*\"pk\":(\\d+).*", "$1");
        return Integer.parseInt(pk);
    }

    // ===== GET /ads (публичный) =====

    @Test
    @DisplayName("GET /ads — публичный доступ → 200")
    void getAllAds_publicAccess_returns200() throws Exception {
        mockMvc.perform(get("/ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").isNumber())
                .andExpect(jsonPath("$.results").isArray());
    }

    // ===== POST /ads =====

    @Test
    @DisplayName("POST /ads — создание объявления → 201")
    @WithMockUser(username = "creator@example.com", roles = "USER")
    void addAd_withAuth_returns201() throws Exception {
        registerAndLogin("creator@example.com");
        String auth = "Basic " + java.util.Base64.getEncoder()
                .encodeToString("creator@example.com:password123".getBytes());

        MockMultipartFile properties = new MockMultipartFile(
                "properties", "", "application/json",
                """
                {"title": "New Phone", "price": 50000, "description": "Brand new phone for sale today"}
                """.getBytes());
        MockMultipartFile image = new MockMultipartFile(
                "image", "phone.jpg", "image/jpeg", new byte[]{0});

        mockMvc.perform(multipart("/ads")
                        .file(properties)
                        .file(image)
                        .header("Authorization", auth))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Phone"))
                .andExpect(jsonPath("$.price").value(50000));
    }

    @Test
    @DisplayName("POST /ads — без авторизации → 401")
    void addAd_withoutAuth_returns401() throws Exception {
        MockMultipartFile properties = new MockMultipartFile(
                "properties", "", "application/json",
                """
                {"title": "New Phone", "price": 50000, "description": "Brand new phone for sale today"}
                """.getBytes());
        MockMultipartFile image = new MockMultipartFile(
                "image", "phone.jpg", "image/jpeg", new byte[]{0});

        mockMvc.perform(multipart("/ads")
                        .file(properties)
                        .file(image))
                .andExpect(status().isUnauthorized());
    }

    // ===== GET /ads/{id} =====

    @Test
    @DisplayName("GET /ads/{id} — получение расширенной информации → 200")
    void getAds_byId_returns200() throws Exception {
        registerAndLogin("detail@example.com");
        String auth = "Basic " + java.util.Base64.getEncoder()
                .encodeToString("detail@example.com:password123".getBytes());
        int adId = createAd("detail@example.com", auth);

        mockMvc.perform(get("/ads/" + adId)
                        .header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(adId))
                .andExpect(jsonPath("$.title").value("Test Ad Title"))
                .andExpect(jsonPath("$.price").value(10000))
                .andExpect(jsonPath("$.authorFirstName").exists())
                .andExpect(jsonPath("$.email").value("detail@example.com"));
    }

    @Test
    @DisplayName("GET /ads/99999 — несуществующее объявление → 404")
    void getAds_nonExistent_returns404() throws Exception {
        registerAndLogin("notfound@example.com");
        String auth = "Basic " + java.util.Base64.getEncoder()
                .encodeToString("notfound@example.com:password123".getBytes());

        mockMvc.perform(get("/ads/99999")
                        .header("Authorization", auth))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ad not found: 99999"));
    }

    // ===== GET /ads/me =====

    @Test
    @DisplayName("GET /ads/me — объявления текущего пользователя → 200")
    void getAdsMe_returns200() throws Exception {
        registerAndLogin("myads@example.com");
        String auth = "Basic " + java.util.Base64.getEncoder()
                .encodeToString("myads@example.com:password123".getBytes());
        createAd("myads@example.com", auth);

        mockMvc.perform(get("/ads/me")
                        .header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.results[0].title").value("Test Ad Title"));
    }

    // ===== PATCH /ads/{id} =====

    @Test
    @DisplayName("PATCH /ads/{id} — владелец обновляет → 200")
    void updateAds_owner_returns200() throws Exception {
        registerAndLogin("owner@example.com");
        String auth = "Basic " + java.util.Base64.getEncoder()
                .encodeToString("owner@example.com:password123".getBytes());
        int adId = createAd("owner@example.com", auth);

        mockMvc.perform(patch("/ads/" + adId)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Updated Title", "price": 20000, "description": "Updated description here"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.price").value(20000));
    }

    @Test
    @DisplayName("PATCH /ads/{id} — чужой пытается обновить → 403")
    void updateAds_stranger_returns403() throws Exception {
        registerAndLogin("owner2@example.com");
        String ownerAuth = "Basic " + java.util.Base64.getEncoder()
                .encodeToString("owner2@example.com:password123".getBytes());
        int adId = createAd("owner2@example.com", ownerAuth);

        registerAndLogin("stranger@example.com");
        String strangerAuth = "Basic " + java.util.Base64.getEncoder()
                .encodeToString("stranger@example.com:password123".getBytes());

        mockMvc.perform(patch("/ads/" + adId)
                        .header("Authorization", strangerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Hacked", "price": 1, "description": "This is a hacked ad content"}
                                """))
                .andExpect(status().isForbidden());
    }

    // ===== DELETE /ads/{id} =====

    @Test
    @DisplayName("DELETE /ads/{id} — владелец удаляет → 204")
    void deleteAds_owner_returns204() throws Exception {
        registerAndLogin("deleter@example.com");
        String auth = "Basic " + java.util.Base64.getEncoder()
                .encodeToString("deleter@example.com:password123".getBytes());
        int adId = createAd("deleter@example.com", auth);

        mockMvc.perform(delete("/ads/" + adId)
                        .header("Authorization", auth))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /ads/{id} — чужой пытается удалить → 403")
    void deleteAds_stranger_returns403() throws Exception {
        registerAndLogin("protected@example.com");
        String ownerAuth = "Basic " + java.util.Base64.getEncoder()
                .encodeToString("protected@example.com:password123".getBytes());
        int adId = createAd("protected@example.com", ownerAuth);

        registerAndLogin("attacker@example.com");
        String attackerAuth = "Basic " + java.util.Base64.getEncoder()
                .encodeToString("attacker@example.com:password123".getBytes());

        mockMvc.perform(delete("/ads/" + adId)
                        .header("Authorization", attackerAuth))
                .andExpect(status().isForbidden());
    }

    // ===== PATCH /ads/{id}/image =====

    @Test
    @DisplayName("PATCH /ads/{id}/image — обновление картинки → 200")
    void updateImage_owner_returns200() throws Exception {
        registerAndLogin("imgowner@example.com");
        String auth = "Basic " + java.util.Base64.getEncoder()
                .encodeToString("imgowner@example.com:password123".getBytes());
        int adId = createAd("imgowner@example.com", auth);

        MockMultipartFile newImage = new MockMultipartFile(
                "image", "new.png", "image/png", new byte[]{0});

        mockMvc.perform(multipart("/ads/" + adId + "/image")
                        .file(newImage)
                        .header("Authorization", auth)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image").value(org.hamcrest.Matchers.startsWith("/images/ads/")));
    }

    @Test
    @DisplayName("PATCH /ads/{id}/image — текстовый файл → 400")
    void updateImage_invalidType_returns400() throws Exception {
        registerAndLogin("imgval@example.com");
        String auth = "Basic " + java.util.Base64.getEncoder()
                .encodeToString("imgval@example.com:password123".getBytes());
        int adId = createAd("imgval@example.com", auth);

        MockMultipartFile fakeFile = new MockMultipartFile(
                "image", "file.txt", "text/plain", "not an image".getBytes());

        mockMvc.perform(multipart("/ads/" + adId + "/image")
                        .file(fakeFile)
                        .header("Authorization", auth)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only PNG, JPEG, GIF images are allowed"));
    }
}
