package com.library.worker.admin;

import com.library.cache.SessionCache;
import com.library.database.DatabaseHandler;
import com.library.worker.admin.enums.LogTarget;
import com.library.worker.admin.enums.LogType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

public class AdminActionsLogger {
    public static void newLog(LogType logType, LogTarget logTarget, String details) {
        try {
            PreparedStatement preparedStatement = DatabaseHandler
                    .getInstance()
                    .getConnection()
                    .prepareStatement("INSERT INTO admin_actions (executor_id, username, full_name, details, type, target, timestamp) VALUES (" +
                                    "'" + SessionCache.userData.get("userID") + "'," +
                                    "'" + SessionCache.userData.get("username") + "'," +
                                    "'" + SessionCache.userData.get("fullName") + "'," +
                                    "'" + details + "'," +
                                    "" + logType.getIndex() + "," +
                                    "" + logTarget.getIndex() + "," +
                                    "" + Instant.now().getEpochSecond() + ")",
                            Statement.RETURN_GENERATED_KEYS);

            preparedStatement.execute();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }
}
