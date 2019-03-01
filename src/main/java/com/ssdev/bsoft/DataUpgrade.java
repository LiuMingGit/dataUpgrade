package com.ssdev.bsoft;

import org.apache.log4j.Logger;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.repository.IRepositoryImporter;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;


/**
 * Hello world!
 */
public class DataUpgrade {
    private static Logger logger = Logger.getLogger(DataUpgrade.class);

    public static void main(String[] args) {
        try {
            fileRepositoryCon();
        } catch (KettleException e) {
            e.printStackTrace();
        }
    }

    /**
     * 配置kettle文件库资源库环境
     **/
    public static KettleFileRepository fileRepositoryCon() throws KettleException {
        String msg;
        //初始化
        EnvUtil.environmentInit();
        KettleEnvironment.init();
        //资源库元对象
        KettleFileRepositoryMeta fileRepositoryMeta = new KettleFileRepositoryMeta("", "dateUpgrade", "数据抽取转换", "src/main/resources");
        // 文件形式的资源库
        KettleFileRepository repo = new KettleFileRepository();
        repo.init(fileRepositoryMeta);
        //连接到资源库
        repo.connect("", "");//默认的连接资源库的用户名和密码
        if (repo.isConnected()) {
            IRepositoryImporter importer = repo.getImporter();
            String[] s = {"dataUpdate.xml"};
            importer.importAll(importer, "src/main/resources", s, repo.findDirectory("/"), true, true, "初始化资源库");
            msg = "kettle文件库资源库【" + "" + "】连接成功";
            logger.info(msg);
            return repo;
        } else {
            msg = "kettle文件库资源库【" + "" + "】连接失败";
            logger.error(msg);
            throw new KettleException(msg);
        }
    }
}
