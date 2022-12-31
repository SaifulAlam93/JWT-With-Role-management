package com.nidle.licence.encryption.repository;

import com.nidle.licence.encryption.entity.LicenceKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EncryptKeyRepository extends JpaRepository<LicenceKey, Long> {
    List<LicenceKey> findAllByActiveTrue();
}
