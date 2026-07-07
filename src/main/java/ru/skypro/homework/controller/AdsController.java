package ru.skypro.homework.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;

@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("/ads")
public class AdsController {

    @GetMapping
    public ResponseEntity<Ads> getAllAds() {
        Ads ads = new Ads();
        return ResponseEntity.ok(ads);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Ad> addAd(
            @RequestPart("properties") CreateOrUpdateAd properties,
            @RequestPart("image") MultipartFile image,
            Authentication authentication) {
        Ad ad = new Ad();
        return ResponseEntity.status(201).body(ad);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExtendedAd> getAds(@PathVariable Integer id) {
        ExtendedAd ad = new ExtendedAd();
        return ResponseEntity.ok(ad);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeAd(@PathVariable Integer id,
                                       Authentication authentication) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Ad> updateAds(
            @PathVariable Integer id,
            @RequestBody CreateOrUpdateAd updateAd,
            Authentication authentication) {
        Ad ad = new Ad();
        return ResponseEntity.ok(ad);
    }

    @GetMapping("/me")
    public ResponseEntity<Ads> getAdsMe(Authentication authentication) {
        Ads ads = new Ads();
        return ResponseEntity.ok(ads);
    }

    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Ad> updateImage(
            @PathVariable Integer id,
            @RequestParam MultipartFile image,
            Authentication authentication) {
        Ad ad = new Ad();
        return ResponseEntity.ok(ad);
    }
}
