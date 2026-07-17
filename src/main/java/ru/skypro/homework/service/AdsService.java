package ru.skypro.homework.service;

import ru.skypro.homework.dto.Ad;
import ru.skypro.homework.dto.Ads;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.dto.ExtendedAd;

public interface AdsService {
    Ads getAllAds();

    Ad addAd(CreateOrUpdateAd properties, String imagePath, String authorEmail);

    ExtendedAd getAd(Integer id);

    void removeAd(Integer id, String authorEmail);

    Ad updateAd(Integer id, CreateOrUpdateAd updateAd, String authorEmail);

    Ads getAdsMe(String authorEmail);

    Ad updateImage(Integer id, String imagePath, String authorEmail);
}
