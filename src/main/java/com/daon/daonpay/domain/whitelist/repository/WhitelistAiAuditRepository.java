package com.daon.daonpay.domain.whitelist.repository;

import com.daon.daonpay.domain.whitelist.entity.WhitelistAiAudit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhitelistAiAuditRepository extends JpaRepository<WhitelistAiAudit, Long> {
    List<WhitelistAiAudit> findByProposalIdOrderByCreatedAtAsc(Long proposalId);
}
