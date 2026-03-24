package com.adetayo.verve_guard.repository;


import com.adetayo.verve_guard.entity.TransactionLog;
import com.adetayo.verve_guard.enums.TransactionStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TransactionLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(TransactionLog log) {
        String sql = "INSERT INTO transaction_logs (" +
                "card_number_hash, card_last_four, merchant_id, amount, " +
                "ip_address, status, fraud_reason" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                sql,
                log.getCardNumberHash(),
                log.getCardLastFour(),
                log.getMerchantId(),
                log.getAmount(),
                log.getIpAddress(),
                log.getStatus() != null ? log.getStatus().name() : null,
                log.getFraudReason()
        );
    }

    public List<TransactionLog> findByCardNumberHash(String cardNumberHash) {
        String sql = "SELECT id, card_number_hash, card_last_four, merchant_id, amount, " +
                "ip_address, status, fraud_reason, created_at " +
                "FROM transaction_logs WHERE card_number_hash = ?";

        return jdbcTemplate.query(sql, new Object[]{cardNumberHash}, new TransactionLogRowMapper());
    }

    private static class TransactionLogRowMapper implements RowMapper<TransactionLog> {

        @Override
        public TransactionLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            TransactionLog log = new TransactionLog();
            log.setId(rs.getLong("id"));
            log.setCardNumberHash(rs.getString("card_number_hash"));
            log.setCardLastFour(rs.getString("card_last_four"));
            log.setMerchantId(rs.getString("merchant_id"));
            log.setAmount(rs.getBigDecimal("amount"));
            log.setIpAddress(rs.getString("ip_address"));

            String statusStr = rs.getString("status");
            if (statusStr != null) {
                log.setStatus(TransactionStatus.valueOf(statusStr));
            }

            log.setFraudReason(rs.getString("fraud_reason"));
            if (rs.getTimestamp("created_at") != null) {
                log.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }

            return log;
        }
    }
}
