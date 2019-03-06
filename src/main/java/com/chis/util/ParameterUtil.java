package com.chis.util;

import com.chis.dateUpgrade.DataUpgrade;

import java.util.HashMap;

public class ParameterUtil {
    //kettle 资源库配置
    private static String KETTLE_PATH;
    private static String KETTLE_ID;
    private static String KETTLE_NAME;
    private static String KETTLE_DESCRIPTION;
    private static String KETTLE_FILENAME;

    //老数据源配置
    private static String OLDDATASOURCE_SERVER;
    private static String OLDDATASOURCE_DATABASE;
    private static String OLDDATASOURCE_PORT;
    private static String OLDDATASOURCE_USERNAME;
    private static String OLDDATASOURCE_PASSWORD;
    private static String OLDDATASOURCE_DRIVER;
    private static String OLDDATASOURCE_URL;

    //新数据源配置
    private static String NEWDATASOURCE_SERVER;
    private static String NEWDATASOURCE_DATABASE;
    private static String NEWDATASOURCE_PORT;
    private static String NEWDATASOURCE_USERNAME;
    private static String NEWDATASOURCE_PASSWORD;
    private static String NEWDATASOURCE_DRIVER;
    private static String NEWDATASOURCE_URL;


    /**
     * 读取配置文件
     */
    static {
        YamlReader reader = YamlReader.getInstance();
        HashMap filerepository = (HashMap) reader.getValuebyKey("kettle", "filerepository");
        KETTLE_PATH = DataUpgrade.class.getClassLoader().getResource("").getPath();
        KETTLE_ID = (String) filerepository.get("id");
        KETTLE_NAME = (String) filerepository.get("name");
        KETTLE_DESCRIPTION = (String) filerepository.get("description");
        KETTLE_FILENAME = (String) filerepository.get("fileName");
        OLDDATASOURCE_URL = (String) reader.getValuebyKey("oldDataSource", "url");
        NEWDATASOURCE_URL = (String) reader.getValuebyKey("newDataSource", "url");
        OLDDATASOURCE_SERVER = OLDDATASOURCE_URL.substring(OLDDATASOURCE_URL.lastIndexOf("@") + 1, OLDDATASOURCE_URL.lastIndexOf(":"));
        OLDDATASOURCE_DATABASE = OLDDATASOURCE_URL.substring(OLDDATASOURCE_URL.lastIndexOf("/") + 1);
        OLDDATASOURCE_PORT = OLDDATASOURCE_URL.substring(OLDDATASOURCE_URL.lastIndexOf(":") + 1, OLDDATASOURCE_URL.lastIndexOf("/"));
        OLDDATASOURCE_USERNAME = (String) reader.getValuebyKey("oldDataSource", "un");
        OLDDATASOURCE_PASSWORD = (String) reader.getValuebyKey("oldDataSource", "pw");
        OLDDATASOURCE_DRIVER = (String) reader.getValuebyKey("oldDataSource", "dn");

        NEWDATASOURCE_SERVER = NEWDATASOURCE_URL.substring(NEWDATASOURCE_URL.lastIndexOf("@") + 1, NEWDATASOURCE_URL.lastIndexOf(":"));
        NEWDATASOURCE_DATABASE = NEWDATASOURCE_URL.substring(NEWDATASOURCE_URL.lastIndexOf("/") + 1);
        NEWDATASOURCE_PORT = NEWDATASOURCE_URL.substring(NEWDATASOURCE_URL.lastIndexOf(":") + 1, NEWDATASOURCE_URL.lastIndexOf("/"));
        NEWDATASOURCE_USERNAME = (String) reader.getValuebyKey("newDataSource", "un");
        NEWDATASOURCE_PASSWORD = (String) reader.getValuebyKey("newDataSource", "pw");
        NEWDATASOURCE_DRIVER = (String) reader.getValuebyKey("newDataSource", "dn");
    }

    public static String getKettlePath() {
        return KETTLE_PATH;
    }

    public static String getKettleId() {
        return KETTLE_ID;
    }

    public static String getKettleName() {
        return KETTLE_NAME;
    }

    public static String getKettleDescription() {
        return KETTLE_DESCRIPTION;
    }

    public static String getKettleFilename() {
        return KETTLE_FILENAME;
    }

    public static String getOlddatasourceServer() {
        return OLDDATASOURCE_SERVER;
    }

    public static String getOlddatasourceDatabase() {
        return OLDDATASOURCE_DATABASE;
    }

    public static String getOlddatasourcePort() {
        return OLDDATASOURCE_PORT;
    }

    public static String getOlddatasourceUsername() {
        return OLDDATASOURCE_USERNAME;
    }

    public static String getOlddatasourcePassword() {
        return OLDDATASOURCE_PASSWORD;
    }

    public static String getOlddatasourceDriver() {
        return OLDDATASOURCE_DRIVER;
    }

    public static String getOlddatasourceUrl() {
        return OLDDATASOURCE_URL;
    }

    public static String getNewdatasourceServer() {
        return NEWDATASOURCE_SERVER;
    }

    public static String getNewdatasourceDatabase() {
        return NEWDATASOURCE_DATABASE;
    }

    public static String getNewdatasourcePort() {
        return NEWDATASOURCE_PORT;
    }

    public static String getNewdatasourceUsername() {
        return NEWDATASOURCE_USERNAME;
    }

    public static String getNewdatasourcePassword() {
        return NEWDATASOURCE_PASSWORD;
    }

    public static String getNewdatasourceDriver() {
        return NEWDATASOURCE_DRIVER;
    }

    public static String getNewdatasourceUrl() {
        return NEWDATASOURCE_URL;
    }

    public static void main(String[] args) {
        System.err.println(ParameterUtil.getKettleFilename());
    }
}
