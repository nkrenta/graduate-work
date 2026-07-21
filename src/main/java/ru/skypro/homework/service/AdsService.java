package ru.skypro.homework.service;

import ru.skypro.homework.dto.Ad;
import ru.skypro.homework.dto.Ads;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.dto.ExtendedAd;

/**
 * Сервис для работы с объявлениями.
 * Предоставляет CRUD-операции: получение, создание, редактирование и удаление объявлений.
 */
public interface AdsService {

    /** Получить все объявления. */
    Ads getAllAds();

    /** Создать новое объявление с изображением. */
    Ad addAd(CreateOrUpdateAd properties, String imagePath, String authorEmail);

    /** Получить расширенную информацию об объявлении по id. */
    ExtendedAd getAd(Integer id);

    /** Удалить объявление (только владелец). */
    void removeAd(Integer id, String authorEmail);

    /** Обновить данные объявления (только владелец). */
    Ad updateAd(Integer id, CreateOrUpdateAd updateAd, String authorEmail);

    /** Получить объявления текущего пользователя. */
    Ads getAdsMe(String authorEmail);

    /** Обновить изображение объявления (только владелец). */
    Ad updateImage(Integer id, String imagePath, String authorEmail);

    /** Проверить, является ли пользователь владельцем объявления. */
    boolean isAdOwner(Integer id, String email);
}
