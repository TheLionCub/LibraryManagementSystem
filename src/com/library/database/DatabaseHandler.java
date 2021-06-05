package com.library.database;

import com.library.LibraryConfig;
import com.library.data.config.DBConfigurationImpl;
import com.library.managers.system.FileManager;
import com.library.utils.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

public class DatabaseHandler {
    public static final LibraryConfig<DBConfigurationImpl> dbConfig = new LibraryConfig<>("data/storage/dbconf.yml", "/resources/config/dbconf.yml", DBConfigurationImpl.class);

    private static int migrationVersion = 1;

    private static DatabaseHandler databaseHandler = null;
    private static Connection connection = null;

    static {
        try {
            createDatabaseConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void databaseInitializer(Connection connection) throws SQLException, ParserConfigurationException, IOException, SAXException {
        InputStream inputStream = DatabaseHandler.class.getResourceAsStream("/resources/config/dbmigration.xml");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputStream);
        document.getDocumentElement().normalize();

        NodeList changeSetList = document.getElementsByTagName("changeSet");

        for (int changeSet = 0; changeSet < changeSetList.getLength(); changeSet++) {
            Node changeSetNode = changeSetList.item(changeSet);
            if (changeSetNode != null && changeSetNode.getNodeType() == Node.ELEMENT_NODE) {
                Element changeSetElement = (Element) changeSetNode;
                migrationVersion = Integer.parseInt(changeSetElement.getAttribute("versionID"));

                NodeList sqlList = changeSetElement.getElementsByTagName("sql");
                for (int sql = 0; sql < sqlList.getLength(); sql++) {
                    Node sqlNode = sqlList.item(sql);
                    if (sqlNode != null && sqlNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element sqlElement = (Element) sqlNode;
                        connection.createStatement().executeUpdate(StringUtils.trimMultilineStr(sqlElement.getTextContent()));
                    }
                }
            }
        }

        dbConfig.getConfiguration().setVersion(migrationVersion);
        dbConfig.saveConfig();
    }

    private static void createDatabaseConnection() {
        migrationVersion = dbConfig.getConfiguration().getVersion();

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + FileManager.createFile("data/storage/database.db"));

            connection.createStatement().execute("PRAGMA case_sensitive_like = 0");

            try {
                databaseInitializer(connection);
            } catch (ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            }
        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
            sqlException.printStackTrace();
        }
    }

    public static DatabaseHandler getInstance() {
        if (databaseHandler == null) {
            databaseHandler = new DatabaseHandler();
        }
        return databaseHandler;
    }

    public static boolean closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            return true;
        }
        return false;
    }

    public Statement newStatement() throws SQLException {
        return connection.createStatement();
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        ResultSet resultSet;

        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery(sql);

        return resultSet;
    }

    public boolean executeUpdate(String sql) throws SQLException {
        Statement statement = connection.createStatement();

        int response = statement.executeUpdate(sql);

        return response != 0;
    }

    public static DatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }

    public Connection getConnection() {
        return connection;
    }
}
