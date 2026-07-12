package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Список объявлений")
public class Ads {
    @Schema(description = "общее количество объявлений")
    private Integer count;

    @Schema(description = "список объявлений")
    private List<Ad> results;

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public List<Ad> getResults() { return results; }
    public void setResults(List<Ad> results) { this.results = results; }
}
