package com.chis.dateUpgrade;

/**
 * 日期：2019年03月01日
 * 作者：刘铭
 * 邮箱：liuming@bsoft.com.cn
 */


import com.chis.util.ParameterUtil;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.IRepositoryImporter;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;


import java.io.*;
import java.sql.*;
import java.util.List;


/**
 * kettle执行主类
 */
public class DataUpgrade {
    private static Logger logger = Logger.getLogger(DataUpgrade.class);

    public static void main(String[] args) throws DocumentException, KettleException {
        DataUpgrade dataUpgrade = new DataUpgrade();
        dataUpgrade.doWork();
    }
    

    /**
     * 配置kettle文件库资源库环境
     **/
    private KettleFileRepository fileRepositoryCon() throws KettleException {
        String msg;
        //初始化
        EnvUtil.environmentInit();
        KettleEnvironment.init();
        //检测文件夹是否存在
        File file = new File(ParameterUtil.getKettlePath() +"/"+ParameterUtil.getKettleName());
        if(!file.exists()){
           file.mkdir();
        }
        //资源库元对象
        KettleFileRepositoryMeta fileRepositoryMeta = new KettleFileRepositoryMeta(ParameterUtil.getKettleId(), ParameterUtil.getKettleName(), ParameterUtil.getKettleDescription(), ParameterUtil.getKettlePath()+"/"+ParameterUtil.getKettleName());
        // 文件形式的资源库
        KettleFileRepository repo = new KettleFileRepository();
        repo.init(fileRepositoryMeta);
        //连接到资源库
        repo.connect("", "");//默认的连接资源库的用户名和密码
        if (repo.isConnected()) {
            msg = "kettle文件库资源库【" + ParameterUtil.getKettleName() + "】连接成功";
            logger.info(msg);
            return repo;
        } else {
            msg = "kettle文件库资源库【" + ParameterUtil.getKettleName() + "】连接失败";
            logger.error(msg);
            throw new KettleException(msg);
        }
    }


    /**
     * 初始导入文件资源库
     *
     * @param repository
     */
    private void initImportRepositort(KettleFileRepository repository) throws KettleException, DocumentException {
        //修改文件资源库中新老数据源配置
        editDataSource();
        IRepositoryImporter importer = repository.getImporter();
        String[] s = {ParameterUtil.getKettleFilename()};
        importer.importAll(importer, ParameterUtil.getKettlePath(), s, repository.findDirectory("/"), true, true, "初始化资源库");
    }

    private void editDataSource() throws DocumentException {
        File file = new File("src/main/resources/" + ParameterUtil.getKettleFilename());
        Document document = new SAXReader().read(new File("src/main/resources/" + ParameterUtil.getKettleFilename()));
        List<Element> transformations = document.getRootElement().element("transformations").elements("transformation");
        for (Element transformation : transformations) {
            //修改kettle读取配置文件位置
            if ("设置数据库配置变量".equals(transformation.element("info").element("name").getText())) {
                List<Element> steps = transformation.elements("step");
                for (Element step : steps) {
                    if ("Yaml 输入".equals(step.element("name").getText())) {
                        step.element("file").element("name").setText(file.getParentFile().getAbsolutePath() + "/oracle.yaml");
                    }
                }
            }
            editDataSource(transformation);
        }
        List<Element> jobs = document.getRootElement().element("jobs").elements("job");
        for (Element job : jobs) {
            editDataSource(job);
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File("src/main/resources/" + ParameterUtil.getKettleFilename()));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("文件重写出失败");
        }
        XMLWriter writer = null;

        try {
            writer = new XMLWriter(fileOutputStream);
            writer.write(document);
            writer.flush();
            writer.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("文件重写出失败");
        } catch (IOException e) {
            throw new IllegalStateException("文件重写出失败");
        }
    }

    @SuppressWarnings("Duplicates")
    private void editDataSource(Element element) {
        //server database port username password
        List<Element> connections = element.elements("connection");
        for (Element con : connections) {
            if ("oldDataSource".equals(con.elementText("name"))) {
                con.element("server").setText(ParameterUtil.getOlddatasourceServer());
                con.element("database").setText(ParameterUtil.getOlddatasourceDatabase());
                con.element("port").setText(ParameterUtil.getOlddatasourcePort());
                con.element("username").setText(ParameterUtil.getOlddatasourceUsername());
                con.element("password").setText(ParameterUtil.getOlddatasourcePassword());
            } else if ("newDataSource".equals(con.elementText("name"))) {
                con.element("server").setText(ParameterUtil.getNewdatasourceServer());
                con.element("database").setText(ParameterUtil.getNewdatasourceDatabase());
                con.element("port").setText(ParameterUtil.getNewdatasourcePort());
                con.element("username").setText(ParameterUtil.getNewdatasourceUsername());
                con.element("password").setText(ParameterUtil.getNewdatasourcePassword());
            }
        }
    }

    /**
     * @return
     * @throws KettleException
     */

    public String doWork() throws KettleException, DocumentException {
        KettleFileRepository repo = fileRepositoryCon();
        initImportRepositort(repo);
        //执行作业
        runJob(repo);
        return "";
    }

    /**
     * 执行作业
     *
     * @param repo
     */
    private void runJob(KettleFileRepository repo) throws KettleException {
        JobMeta jobMeta = new JobMeta(ParameterUtil.getKettlePath()+'/'+ ParameterUtil.getKettleName()+ '/' + "dataupdate.kjb", repo);
        Job job = new Job(repo, jobMeta);
        /*Scheduler instance = ProgressSchedule.getInstance();
        ProgressSchedule progressSchedule = new ProgressSchedule();
        JobKey jobKey = new JobKey("dataupdate","kettle");
        try {
            progressSchedule.start(jobKey);
        } catch (SchedulerException e) {
            throw new KettleException("进度监控作业运行失败",e);
        }*/
        job.start();
        job.waitUntilFinished();
        if (job.getErrors() > 0) {
            throw new KettleException("执行作业失败!");
        }

       /* try {
            instance.deleteJob(jobKey);
            instance.shutdown();
        } catch (SchedulerException e) {
            throw new KettleException("进度监控作业运行失败",e);
        }*/
    }

    /**
     *  回归处理标识字段
     * @param rows
     */
    private void updateStat(List<RowMetaAndData> rows) throws KettleException {
        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement psc = null;
        ResultSet resultSet = null;
        try{
            try {
                Class.forName(ParameterUtil.getOlddatasourceDriver());
                conn = DriverManager.getConnection(ParameterUtil.getOlddatasourceUrl(), ParameterUtil.getOlddatasourceUsername(), ParameterUtil.getOlddatasourcePassword());

                boolean autoCommit = conn.getAutoCommit();
                conn.setAutoCommit(false);

                String columnSql = "select  col.column_name as column_name from user_constraints con,user_cons_columns col where con.constraint_name=col.constraint_name and con.constraint_type='P' and col.table_name=upper(?)";
                String table_name = rows.get(0).getString(1,"");
                psc = conn.prepareStatement(columnSql);
                psc.setString(1,table_name);
                resultSet = psc.executeQuery();
                String column_name = null;
                if(resultSet.next())
                    column_name = resultSet.getString("column_name");
                else{
                    return;
                }
                String sql = "update "+table_name.toUpperCase()+" set dataUpgradeStat = 1 where "+column_name+" = ?";
                ps = conn.prepareStatement(sql);
                rows.remove(0);
                String column_value = "";
                for (RowMetaAndData row : rows){
                    column_value = row.getString(column_name,"");
                    ps.setString(1,column_value);
                    ps.addBatch();
                }
                ps.executeBatch();
                ps.executeUpdate();
                conn.commit();
                conn.setAutoCommit(autoCommit);
            } catch (ClassNotFoundException e) {
                conn.rollback();
                throw new KettleException("回归处理标识字段失败");
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                throw new KettleException("回归处理标识字段失败",e);
            } finally {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            }
        }catch (SQLException e){
            throw new KettleException("回归处理标识字段失败");
        }



    }

}
