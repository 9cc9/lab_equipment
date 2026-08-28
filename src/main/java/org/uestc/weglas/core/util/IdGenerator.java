package org.uestc.weglas.core.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
@Slf4j
public class IdGenerator {

    @Autowired
    private DataSource dataSource;

    public enum EntityType {
        USER("USR"),
        EQUIPMENT("EQP"),
        ROOM("ROM"),
        CHANGE_LOG("CHG");

        private final String prefix;

        EntityType(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    public String generate(EntityType entityType) {
        long autoIncrementId = getNextSequence(entityType.getPrefix());
        return String.format("%s00%010d", entityType.getPrefix(), autoIncrementId);
    }

    private long getNextSequence(String entityTypePrefix) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement updatePs = connection.prepareStatement(
                        "UPDATE id_sequence SET current_value = LAST_INSERT_ID(current_value + step) WHERE entity_type = ?")) {
                    updatePs.setString(1, entityTypePrefix);
                    int updateCount = updatePs.executeUpdate();
                    if (updateCount == 0) {
                        try (PreparedStatement insertPs = connection.prepareStatement(
                                "INSERT INTO id_sequence (entity_type, current_value, step) VALUES (?, LAST_INSERT_ID(1), 1) ON DUPLICATE KEY UPDATE current_value = LAST_INSERT_ID(current_value + step)")) {
                            insertPs.setString(1, entityTypePrefix);
                            insertPs.executeUpdate();
                        }
                    }
                }
                try (Statement selectStmt = connection.createStatement();
                     ResultSet resultSet = selectStmt.executeQuery("SELECT LAST_INSERT_ID()")) {
                    if (resultSet.next()) {
                        long sequenceValue = resultSet.getLong(1);
                        connection.commit();
                        return sequenceValue;
                    }
                    connection.rollback();
                    throw new RuntimeException("Failed to read sequence: " + entityTypePrefix);
                }
            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException("Failed to fetch sequence: " + entityTypePrefix, e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect id_sequence", e);
        }
    }
}
