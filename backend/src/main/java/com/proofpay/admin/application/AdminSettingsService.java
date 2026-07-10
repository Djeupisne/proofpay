package com.proofpay.admin.application;

import com.proofpay.admin.domain.AdminSetting;
import com.proofpay.admin.infrastructure.AdminSettingRepository;
import com.proofpay.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Gestion des paramètres métier : frais, délais, catégories (§8.8).
 * Les valeurs sont lues en base à chaque appel (pas de cache) afin qu'une
 * modification via PUT /api/admin/settings/{key} soit immédiatement effective
 * sans redéploiement — c'est tout l'intérêt de cette table par rapport à une
 * simple valeur figée dans application.yml.
 */
@Service
public class AdminSettingsService {

    private final AdminSettingRepository repository;

    public AdminSettingsService(AdminSettingRepository repository) {
        this.repository = repository;
    }

    public List<AdminSetting> listAll() {
        return repository.findAll();
    }

    public AdminSetting update(String key, String value) {
        AdminSetting setting = repository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException("Paramètre introuvable : " + key));
        setting.setSettingValue(value);
        setting.setUpdatedAt(Instant.now());
        return repository.save(setting);
    }

    /** Lecture typée avec repli sur une valeur par défaut si le paramètre est absent. */
    public long getLong(String key, long fallback) {
        return repository.findById(key)
                .map(s -> Long.parseLong(s.getSettingValue()))
                .orElse(fallback);
    }

    public BigDecimal getDecimal(String key, BigDecimal fallback) {
        return repository.findById(key)
                .map(s -> new BigDecimal(s.getSettingValue()))
                .orElse(fallback);
    }
}
