package com.daon.daonpay.domain.whitelist.repository;

import com.daon.daonpay.domain.whitelist.entity.WhitelistChangeProposal;
import com.daon.daonpay.domain.whitelist.entity.WhitelistProposalStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhitelistChangeProposalRepository extends JpaRepository<WhitelistChangeProposal, Long> {
    List<WhitelistChangeProposal> findByStatusOrderByCreatedAtDesc(WhitelistProposalStatus status);
}
