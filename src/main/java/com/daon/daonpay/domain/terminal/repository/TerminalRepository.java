package com.daon.daonpay.domain.terminal.repository;

import com.daon.daonpay.domain.terminal.entity.Terminal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerminalRepository extends JpaRepository<Terminal, Long> {

    Optional<Terminal> findByTerminalCode(String terminalCode);
}