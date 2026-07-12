package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Данные для создания или обновления комментария")
public class CreateOrUpdateComment {
    @Schema(description = "текст комментария", required = true, minLength = 8, maxLength = 64)
    private String text;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
