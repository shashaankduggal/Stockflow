package com.stockflow.audit;

import com.stockflow.product.Product;
import com.stockflow.user.User;
import com.stockflow.warehouse.Warehouse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void logInventoryAction(
            String action,
            Product product,
            Warehouse sourceWarehouse,
            Warehouse destinationWarehouse,
            Integer quantity,
            String remarks) {

        String details = buildDetails(action, product, sourceWarehouse, destinationWarehouse, quantity, remarks);
        AuditLog auditLog = new AuditLog(
                currentUserName(),
                currentUserEmail(),
                action,
                details);

        auditLogRepository.save(auditLog);
    }

    private String currentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user.getFullName();
        }
        return "System";
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user.getEmail();
        }
        return "system@stockflow.local";
    }

    private String buildDetails(
            String action,
            Product product,
            Warehouse sourceWarehouse,
            Warehouse destinationWarehouse,
            Integer quantity,
            String remarks) {

        StringBuilder builder = new StringBuilder();
        builder.append(action)
                .append(" ")
                .append(quantity)
                .append(" unit(s) of ")
                .append(product.getName())
                .append(" [")
                .append(product.getSku())
                .append("]");

        if (sourceWarehouse != null) {
            builder.append(" from ").append(sourceWarehouse.getName());
        }

        if (destinationWarehouse != null) {
            builder.append(" to ").append(destinationWarehouse.getName());
        }

        if (remarks != null && !remarks.isBlank()) {
            builder.append(". Remarks: ").append(remarks.trim());
        }

        return builder.toString();
    }
}
