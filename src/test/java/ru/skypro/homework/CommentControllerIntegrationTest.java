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

import java.io.InputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommentControllerIntegrationTest {

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
                                    "firstName": "Comment",
                                    "lastName": "Tester",
                                    "phone": "+7(900)333-44-55",
                                    "role": "USER"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());
    }

    private int createAd(String email) throws Exception {
        String auth = encodeAuth(email);
        MockMultipartFile properties = new MockMultipartFile(
                "properties", "", "application/json",
                """
                {"title": "Comment Test Ad", "price": 15000, "description": "Ad for testing comments functionality"}
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
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String pk = body.replaceAll(".*\"pk\":(\\d+).*", "$1");
        return Integer.parseInt(pk);
    }

    private int addComment(String email, int adId) throws Exception {
        String auth = encodeAuth(email);
        var result = mockMvc.perform(post("/ads/" + adId + "/comments")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text": "This is a test comment for the advertisement"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").isNumber())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String pk = body.replaceAll(".*\"pk\":(\\d+).*", "$1");
        return Integer.parseInt(pk);
    }

    // ===== GET /ads/{id}/comments =====

    @Test
    @DisplayName("GET /ads/{id}/comments — получение комментариев → 200")
    void getComments_returns200() throws Exception {
        register("getcomm@example.com");
        String auth = encodeAuth("getcomm@example.com");
        int adId = createAd("getcomm@example.com");
        addComment("getcomm@example.com", adId);

        mockMvc.perform(get("/ads/" + adId + "/comments")
                        .header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.results[0].text").value("This is a test comment for the advertisement"));
    }

    // ===== POST /ads/{id}/comments =====

    @Test
    @DisplayName("POST /ads/{id}/comments — добавление комментария → 200")
    void addComment_returns200() throws Exception {
        register("addcomm@example.com");
        String auth = encodeAuth("addcomm@example.com");
        int adId = createAd("addcomm@example.com");

        mockMvc.perform(post("/ads/" + adId + "/comments")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text": "Great ad, I am very interested in buying this item"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great ad, I am very interested in buying this item"))
                .andExpect(jsonPath("$.pk").isNumber())
                .andExpect(jsonPath("$.author").isNumber())
                .andExpect(jsonPath("$.createdAt").isNumber());
    }

    @Test
    @DisplayName("POST /ads/{id}/comments — без авторизации → 401")
    void addComment_withoutAuth_returns401() throws Exception {
        register("nocommauth@example.com");
        int adId = createAd("nocommauth@example.com");

        mockMvc.perform(post("/ads/" + adId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text": "This comment should fail because no auth is provided"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    // ===== PATCH /ads/{id}/comments/{commentId} =====

    @Test
    @DisplayName("PATCH /ads/{id}/comments/{commentId} — владелец обновляет → 200")
    void updateComment_owner_returns200() throws Exception {
        register("updcomm@example.com");
        String auth = encodeAuth("updcomm@example.com");
        int adId = createAd("updcomm@example.com");
        int commentId = addComment("updcomm@example.com", adId);

        mockMvc.perform(patch("/ads/" + adId + "/comments/" + commentId)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text": "Updated comment text for the advertisement post"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated comment text for the advertisement post"));
    }

    @Test
    @DisplayName("PATCH /ads/{id}/comments/{commentId} — чужой пытается → 403")
    void updateComment_stranger_returns403() throws Exception {
        register("commowner@example.com");
        String ownerAuth = encodeAuth("commowner@example.com");
        int adId = createAd("commowner@example.com");
        int commentId = addComment("commowner@example.com", adId);

        register("commhacker@example.com");
        String hackerAuth = encodeAuth("commhacker@example.com");

        mockMvc.perform(patch("/ads/" + adId + "/comments/" + commentId)
                        .header("Authorization", hackerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text": "This should fail with 403 forbidden access"}
                                """))
                .andExpect(status().isForbidden());
    }

    // ===== DELETE /ads/{id}/comments/{commentId} =====

    @Test
    @DisplayName("DELETE /ads/{id}/comments/{commentId} — владелец удаляет → 204")
    void deleteComment_owner_returns204() throws Exception {
        register("delcomm@example.com");
        String auth = encodeAuth("delcomm@example.com");
        int adId = createAd("delcomm@example.com");
        int commentId = addComment("delcomm@example.com", adId);

        mockMvc.perform(delete("/ads/" + adId + "/comments/" + commentId)
                        .header("Authorization", auth))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /ads/{id}/comments/{commentId} — чужой пытается → 403")
    void deleteComment_stranger_returns403() throws Exception {
        register("delcommowner@example.com");
        String ownerAuth = encodeAuth("delcommowner@example.com");
        int adId = createAd("delcommowner@example.com");
        int commentId = addComment("delcommowner@example.com", adId);

        register("delcommhacker@example.com");
        String hackerAuth = encodeAuth("delcommhacker@example.com");

        mockMvc.perform(delete("/ads/" + adId + "/comments/" + commentId)
                        .header("Authorization", hackerAuth))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /ads/{id}/comments/{commentId} — после удаления комментариев нет → count=0")
    void deleteComment_afterDelete_countIsZero() throws Exception {
        register("countcheck@example.com");
        String auth = encodeAuth("countcheck@example.com");
        int adId = createAd("countcheck@example.com");
        int commentId = addComment("countcheck@example.com", adId);

        mockMvc.perform(delete("/ads/" + adId + "/comments/" + commentId)
                        .header("Authorization", auth))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/ads/" + adId + "/comments")
                        .header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }
}
