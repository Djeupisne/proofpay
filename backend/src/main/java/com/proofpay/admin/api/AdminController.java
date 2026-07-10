package com.proofpay.admin.api;

import com.proofpay.admin.application.AdminReportingService;
import com.proofpay.admin.application.AdminSettingsService;
import com.proofpay.admin.application.AdminStatsService;
import com.proofpay.admin.domain.AdminSetting;
import com.proofpay.dispute.application.DisputeService;
import com.proofpay.dispute.domain.Dispute;
import com.proofpay.transaction.domain.Transaction;
import com.proofpay.user.application.UserService;
import com.proofpay.user.domain.User;
import com.proofpay.user.domain.UserRole;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Tous les endpoints sont réservés au rôle ADMIN (cf. SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminReportingService reportingService;
    private final AdminSettingsService settingsService;
    private final AdminStatsService statsService;
    private final UserService userService;
    private final DisputeService disputeService;

    public AdminController(AdminReportingService reportingService,
                            AdminSettingsService settingsService,
                            AdminStatsService statsService,
                            UserService userService,
                            DisputeService disputeService) {
        this.reportingService = reportingService;
        this.settingsService = settingsService;
        this.statsService = statsService;
        this.userService = userService;
        this.disputeService = disputeService;
    }

    /** Vue d'ensemble pour le tableau de bord support (§8.8). */
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return statsService.compute();
    }

    @GetMapping("/transactions")
    public List<Transaction> searchTransactions(@RequestParam(required = false) UUID userId,
                                                 @RequestParam(required = false) String publicRef,
                                                 @PageableDefault(size = 20) Pageable pageable) {
        if (publicRef != null) {
            return List.of(reportingService.searchByRef(publicRef));
        }
        return reportingService.searchByUser(userId, pageable).getContent();
    }

    @GetMapping("/settings")
    public List<AdminSetting> settings() {
        return settingsService.listAll();
    }

    @PutMapping("/settings/{key}")
    public AdminSetting updateSetting(@PathVariable String key, @RequestBody Map<String, String> body) {
        return settingsService.update(key, body.get("value"));
    }

    /** Alimente l'écran support de traitement des litiges (§8.6), paginé (§13). */
    @GetMapping("/disputes")
    public List<Dispute> openDisputes(@PageableDefault(size = 20) Pageable pageable) {
        return disputeService.listOpen(pageable);
    }

    /** Règle métier #22 : blocage manuel d'un compte frauduleux/abusif. */
    @PostMapping("/users/{id}/suspend")
    public void suspendUser(@PathVariable("id") UUID userId) {
        userService.suspend(userId);
    }

    /** Gestion des rôles (USER/ADMIN/SUPPORT) — permet de promouvoir un membre du support. */
    @PostMapping("/users/{id}/role")
    public User updateUserRole(@PathVariable("id") UUID userId, @RequestBody Map<String, String> body) {
        UserRole role = UserRole.valueOf(body.get("role"));
        return userService.updateRole(userId, role);
    }
}
