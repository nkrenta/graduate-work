package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Комментарий")
public class Comment {
    @Schema(description = "id автора комментария")
    private Integer author;

    @Schema(description = "ссылка на аватар автора комментария")
    private String authorImage;

    @Schema(description = "имя создателя комментария")
    private String authorFirstName;

    @Schema(description = "дата и время создания комментария в миллисекундах с 00:00:00 01.01.1970")
    private Long createdAt;

    @Schema(description = "id комментария")
    private Integer pk;

    @Schema(description = "текст комментария")
    private String text;

    public Integer getAuthor() { return author; }
    public void setAuthor(Integer author) { this.author = author; }

    public String getAuthorImage() { return authorImage; }
    public void setAuthorImage(String authorImage) { this.authorImage = authorImage; }

    public String getAuthorFirstName() { return authorFirstName; }
    public void setAuthorFirstName(String authorFirstName) { this.authorFirstName = authorFirstName; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Integer getPk() { return pk; }
    public void setPk(Integer pk) { this.pk = pk; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
