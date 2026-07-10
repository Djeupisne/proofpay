package com.proofpay.admin.infrastructure;

import com.proofpay.admin.domain.AdminSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminSettingRepository extends JpaRepository<AdminSetting, String> {
}
