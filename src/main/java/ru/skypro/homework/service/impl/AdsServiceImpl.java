package ru.skypro.homework.service.impl;

import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.Ad;
import ru.skypro.homework.dto.Ads;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.dto.ExtendedAd;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.service.AdsService;
import ru.skypro.homework.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdsServiceImpl implements AdsService {

    private static final String AD_NOT_FOUND = "Ad not found: ";
    private static final String NOT_AUTHORIZED_TO_DELETE = "Not authorized to delete this ad";
    private static final String NOT_AUTHORIZED_TO_UPDATE = "Not authorized to update this ad";

    private final AdRepository adRepository;
    private final AdMapper adMapper;
    private final UserService userService;

    public AdsServiceImpl(AdRepository adRepository, AdMapper adMapper, UserService userService) {
        this.adRepository = adRepository;
        this.adMapper = adMapper;
        this.userService = userService;
    }

    @Override
    public Ads getAllAds() {
        List<AdEntity> entities = adRepository.findAll();
        List<Ad> ads = entities.stream()
                .map(adMapper::toDto)
                .collect(Collectors.toList());

        Ads result = new Ads();
        result.setCount(ads.size());
        result.setResults(ads);
        return result;
    }

    @Override
    public Ad addAd(CreateOrUpdateAd properties, String imagePath, String authorEmail) {
        UserEntity author = userService.getEntityByEmail(authorEmail);
        AdEntity entity = adMapper.toEntity(properties);
        entity.setAuthor(author);
        entity.setImage(imagePath);
        AdEntity saved = adRepository.save(entity);
        return adMapper.toDto(saved);
    }

    @Override
    public ExtendedAd getAd(Integer id) {
        AdEntity entity = adRepository.findById(id.longValue())
                .orElseThrow(() -> new RuntimeException(AD_NOT_FOUND + id));
        return adMapper.toExtendedDto(entity);
    }

    @Override
    public void removeAd(Integer id, String authorEmail) {
        AdEntity entity = adRepository.findById(id.longValue())
                .orElseThrow(() -> new RuntimeException(AD_NOT_FOUND + id));
        if (!entity.getAuthor().getEmail().equals(authorEmail)) {
            throw new RuntimeException(NOT_AUTHORIZED_TO_DELETE);
        }
        adRepository.delete(entity);
    }

    @Override
    public Ad updateAd(Integer id, CreateOrUpdateAd updateAd, String authorEmail) {
        AdEntity entity = adRepository.findById(id.longValue())
                .orElseThrow(() -> new RuntimeException(AD_NOT_FOUND + id));
        if (!entity.getAuthor().getEmail().equals(authorEmail)) {
            throw new RuntimeException(NOT_AUTHORIZED_TO_UPDATE);
        }
        adMapper.updateEntityFromDto(updateAd, entity);
        AdEntity saved = adRepository.save(entity);
        return adMapper.toDto(saved);
    }

    @Override
    public Ads getAdsMe(String authorEmail) {
        UserEntity author = userService.getEntityByEmail(authorEmail);
        List<AdEntity> entities = adRepository.findByAuthorId(author.getId());
        List<Ad> ads = entities.stream()
                .map(adMapper::toDto)
                .collect(Collectors.toList());

        Ads result = new Ads();
        result.setCount(ads.size());
        result.setResults(ads);
        return result;
    }

    @Override
    public Ad updateImage(Integer id, String imagePath, String authorEmail) {
        AdEntity entity = adRepository.findById(id.longValue())
                .orElseThrow(() -> new RuntimeException(AD_NOT_FOUND + id));
        if (!entity.getAuthor().getEmail().equals(authorEmail)) {
            throw new RuntimeException(NOT_AUTHORIZED_TO_UPDATE);
        }
        entity.setImage(imagePath);
        AdEntity saved = adRepository.save(entity);
        return adMapper.toDto(saved);
    }
}
