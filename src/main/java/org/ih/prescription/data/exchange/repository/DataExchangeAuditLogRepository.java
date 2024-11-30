package org.ih.prescription.data.exchange.repository;


import org.ih.prescription.data.exchange.model.DataExchangeAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataExchangeAuditLogRepository  extends JpaRepository<DataExchangeAuditLog, Integer> {

}
