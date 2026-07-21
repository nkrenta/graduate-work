package ru.skypro.homework.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.Ad;
import ru.skypro.homework.dto.Ads;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.dto.ExtendedAd;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.CommentEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.service.AdsService;
import ru.skypro.homework.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация {@link AdsService}.
 * Работает с репозиторием объявлений, маппингом DTO и управлением файлами изображений.
 */
@Service("adsService")
public class AdsServiceImpl implements AdsService {

    private static final String AD_NOT_FOUND = "Ad not found: ";

    private final AdRepository adRepository;
    private final AdMapper adMapper;
    private final UserService userService;
    private final CommentRepository commentRepository;

    @Value("${upload.path:uploads}")
    private String uploadPath;

    public AdsServiceImpl(AdRepository adRepository, AdMapper adMapper, UserService userService,
                          CommentRepository commentRepository) {
        this.adRepository = adRepository;
        this.adMapper = adMapper;
        this.userService = userService;
        this.commentRepository = commentRepository;
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
    @PreAuthorize("@adsService.isAdOwner(#id, authentication.name)")
    public void removeAd(Integer id, String authorEmail) {
        AdEntity entity = adRepository.findById(id.longValue())
                .orElseThrow(() -> new RuntimeException(AD_NOT_FOUND + id));
        if (entity.getImage() != null) {
            Path imagePath = Path.of(uploadPath, entity.getImage().replaceFirst("^/images/", ""));
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                // логируем, но не прерываем удаление объявления
            }
        }
        List<CommentEntity> comments = commentRepository.findByAdIdOrderByCreatedAtDesc(id.longValue());
        commentRepository.deleteAll(comments);
        adRepository.delete(entity);
    }

    @Override
    @PreAuthorize("@adsService.isAdOwner(#id, authentication.name)")
    public Ad updateAd(Integer id, CreateOrUpdateAd updateAd, String authorEmail) {
        AdEntity entity = adRepository.findById(id.longValue())
                .orElseThrow(() -> new RuntimeException(AD_NOT_FOUND + id));
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
    @PreAuthorize("@adsService.isAdOwner(#id, authentication.name)")
    public Ad updateImage(Integer id, String imagePath, String authorEmail) {
        AdEntity entity = adRepository.findById(id.longValue())
                .orElseThrow(() -> new RuntimeException(AD_NOT_FOUND + id));
        if (entity.getImage() != null) {
            Path oldPath = Path.of(uploadPath, entity.getImage().replaceFirst("^/images/", ""));
            try {
                Files.deleteIfExists(oldPath);
            } catch (IOException e) {
                // логируем, но не прерываем обновление
            }
        }
        entity.setImage(imagePath);
        AdEntity saved = adRepository.save(entity);
        return adMapper.toDto(saved);
    }

    @Override
    public boolean isAdOwner(Integer id, String email) {
        return adRepository.findById(id.longValue())
                .map(entity -> entity.getAuthor().getEmail().equals(email))
                .orElse(false);
    }
}
