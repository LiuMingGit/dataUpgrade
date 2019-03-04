package com.chis.dateUpgrade;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.repository.IRepositoryImporter;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;

import java.io.*;
import java.util.HashMap;
import java.util.List;


/**
 * 日期：2019年03月01日
 * 作者：刘铭
 * 邮箱：liuming@bsoft.com.cn
 */

/**
 *kettle执行主类
 */
public class DataUpgrade {
    private static Logger logger = Logger.getLogger(DataUpgrade.class);

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

    //新数据源配置
    private static String NEWDATASOURCE_SERVER;
    private static String NEWDATASOURCE_DATABASE;
    private static String NEWDATASOURCE_PORT;
    private static String NEWDATASOURCE_USERNAME;
    private static String NEWDATASOURCE_PASSWORD;


    public static void main(String[] args) throws DocumentException, KettleException {
       /* try {
            //fileRepositoryCon();
        } catch (KettleException e) {
            e.printStackTrace();
        }*/
       getProperties();
        editDataSource();
    }

    /**
     * 读取配置文件
     */
    @SuppressWarnings("Duplicates")
    private static void getProperties() {
        YamlReader reader = YamlReader.getInstance();
        HashMap filerepository = (HashMap) reader.getValuebyKey("kettle", "filerepository");
        KETTLE_PATH = (String) filerepository.get("path");
        KETTLE_ID = (String) filerepository.get("id");
        KETTLE_NAME = (String) filerepository.get("name");
        KETTLE_DESCRIPTION = (String) filerepository.get("description");
        KETTLE_FILENAME = (String) filerepository.get("fileName");
        String old_url = (String) reader.getValuebyKey("oldDataSource", "url");
        String new_url = (String) reader.getValuebyKey("newDataSource", "url");
        OLDDATASOURCE_SERVER = old_url.substring(old_url.lastIndexOf("@")+1, old_url.lastIndexOf(":"));
        OLDDATASOURCE_DATABASE = old_url.substring(old_url.lastIndexOf("/") + 1);
        OLDDATASOURCE_PORT = old_url.substring(old_url.lastIndexOf(":") + 1, old_url.lastIndexOf("/"));
        OLDDATASOURCE_USERNAME = (String) reader.getValuebyKey("oldDataSource", "un");
        OLDDATASOURCE_PASSWORD = (String) reader.getValuebyKey("oldDataSource", "pw");

        NEWDATASOURCE_SERVER = new_url.substring(new_url.lastIndexOf("@" )+1, new_url.lastIndexOf(":"));
        NEWDATASOURCE_DATABASE = new_url.substring(new_url.lastIndexOf("/") + 1);
        NEWDATASOURCE_PORT = new_url.substring(new_url.lastIndexOf(":") + 1, new_url.lastIndexOf("/"));
        NEWDATASOURCE_USERNAME = (String) reader.getValuebyKey("newDataSource", "un");
        NEWDATASOURCE_PASSWORD = (String) reader.getValuebyKey("newDataSource", "pw");
    }


    /**
     * 配置kettle文件库资源库环境
     **/
    private static KettleFileRepository fileRepositoryCon() throws KettleException {
        getProperties();
        String msg;
        //初始化
        EnvUtil.environmentInit();
        KettleEnvironment.init();
        //资源库元对象
        KettleFileRepositoryMeta fileRepositoryMeta = new KettleFileRepositoryMeta(KETTLE_ID, KETTLE_NAME, KETTLE_DESCRIPTION, KETTLE_PATH);
        // 文件形式的资源库
        KettleFileRepository repo = new KettleFileRepository();
        repo.init(fileRepositoryMeta);
        //连接到资源库
        repo.connect("", "");//默认的连接资源库的用户名和密码
        if (repo.isConnected()) {
            msg = "kettle文件库资源库【" + KETTLE_NAME + "】连接成功";
            logger.info(msg);
            return repo;
        } else {
            msg = "kettle文件库资源库【" + KETTLE_NAME + "】连接失败";
            logger.error(msg);
            throw new KettleException(msg);
        }
    }

    /**
     * 判断资源库是否已经导入
     *
     * @return
     */
    private static boolean repositoryIsImport(KettleFileRepository repository) {
        boolean exists = false;
        try {
            exists = repository.exists("dataupdate", repository.findDirectory("/"), RepositoryObjectType.valueOf("TRANSFORMATION"));
        } catch (KettleException e) {
            e.printStackTrace();
        }
        return exists;
    }

    /**
     * 初始导入文件资源库
     *
     * @param repository
     */
    private static void initImportRepositort(KettleFileRepository repository) throws KettleException, DocumentException {
        //修改文件资源库中新老数据源配置
        IRepositoryImporter importer = repository.getImporter();
        String[] s = {KETTLE_FILENAME};
        importer.importAll(importer, KETTLE_PATH, s, repository.findDirectory("/"), true, true, "初始化资源库");
        editDataSource();
    }

    private static void editDataSource() throws DocumentException {
        File file = new File("src/main/resources/"+KETTLE_FILENAME);
        Document document = new SAXReader().read(file);
        List<Element> transformations = document.getRootElement().element("transformations").elements("transformation");
        for (Element transformation : transformations) {
            //修改kettle读取配置文件位置
            if("设置数据库配置变量".equals(transformation.element("info").element("name").getText())){
                transformation.element("step").element("file").element("name").setText(file.getParentFile().getAbsolutePath()+"/oraacle.yaml");
            }
            editDataSource(transformation);
        }
        List<Element> jobs = document.getRootElement().element("jobs").elements("job");
        for (Element job : jobs) {
            editDataSource(job);
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("文件重写出失败");
        }
        XMLWriter writer = null;

        try {
            writer = new XMLWriter(fileOutputStream);
            writer.write(document);
            writer.close();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("文件重写出失败");
        } catch (IOException e) {
            throw new IllegalStateException("文件重写出失败");
        }
    }

    @SuppressWarnings("Duplicates")
    private static void editDataSource(Element element) {
        //server database port username password
        List<Element> connections = element.elements("connection");
        for (Element con : connections) {
            if ("bsoft6".equals(con.elementText("name"))) {
                con.element("server").setText(OLDDATASOURCE_SERVER);
                con.element("database").setText(OLDDATASOURCE_DATABASE);
                con.element("port").setText(OLDDATASOURCE_PORT);
                con.element("username").setText(OLDDATASOURCE_USERNAME);
                con.element("password").setText(OLDDATASOURCE_PASSWORD);
            } else if ("bsofttset".equals(con.elementText("name"))) {
                con.element("server").setText(NEWDATASOURCE_SERVER);
                con.element("database").setText(NEWDATASOURCE_DATABASE);
                con.element("port").setText(NEWDATASOURCE_PORT);
                con.element("username").setText(NEWDATASOURCE_USERNAME);
                con.element("password").setText(NEWDATASOURCE_PASSWORD);
            }
        }
    }

    /**
     * @return
     * @throws KettleException
     */

    public static String doWork() throws KettleException, DocumentException {
        KettleFileRepository repo = fileRepositoryCon();
        boolean exists = repositoryIsImport(repo);
        if (!exists) {
            initImportRepositort(repo);
        }
        return "";
    }







}
