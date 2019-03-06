package com.chis.quartz;

import com.chis.pojo.Progress;
import com.chis.pojo.Progresz;
import com.chis.util.ParameterUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.*;
import java.util.HashMap;
import java.util.Set;

public class ProgressJob implements Job {
    private  Connection oldConn;
    private  Connection newConn;


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        try {
            getConn();
            String tableSql = "select TABLENAME from DATA_EXTRACTION";
            Statement statement = null;
            Statement oldSt = null;
            Statement newSt = null;
            ResultSet resultSet = null;
            try {
                HashMap<String, Progresz> progreszs = Progress.getProgreszs();
                if(!Progress.getIsInit()){
                    //初始化所有进度
                    statement = oldConn.createStatement();
                    resultSet = statement.executeQuery(tableSql);
                    while (resultSet.next()){
                        String TABLENAME = resultSet.getString(1);
                        if(progreszs.get(TABLENAME) == null){
                            progreszs.put(TABLENAME,new Progresz(TABLENAME));
                        }
                    }
                    Progress.setIsInit(true);
                }

                //进度缓存
                String totalSql = "select count(1) as oldCon from ";
                String transportNumberSql = "select count(1) as newCon from ";
                oldSt = oldConn.createStatement();
                newSt = newConn.createStatement();
                Set<String> set = progreszs.keySet();
                for(String tableName:set){
                    ResultSet oldRs = null;
                    ResultSet newRs = null;
                    try{
                        oldRs = oldSt.executeQuery(totalSql + tableName+" where dataUpgradeStat = 0");
                        newRs = newSt.executeQuery(transportNumberSql+tableName+" where dataUpgradeStat = 0");
                        long oldCon = 0;
                        long newCon = 0;
                        if(oldRs.next())
                            oldCon = oldRs.getLong(1);
                        if(newRs.next())
                            newCon = newRs.getLong(1);
                        progreszs.get(tableName).setTotal(oldCon);
                        progreszs.get(tableName).setTransportNumber(newCon);
                        System.err.println(progreszs);
                    }catch (Exception e){
                        throw new JobExecutionException("获取抽取进度失败",e);
                    }finally {
                        if(oldRs != null)
                            oldRs.close();
                        if(newRs != null)
                            newRs.close();
                    }
                }

            } catch (SQLException e) {
                throw new JobExecutionException("获取抽取进度失败",e);
            }finally {
                if(oldSt != null)
                    oldSt.close();
                if(newSt != null)
                    newSt.close();
                if(oldConn != null)
                    oldConn.close();
                if(newConn != null)
                    newConn.close();
            }
        }catch (Exception e){
            throw new JobExecutionException("获取抽取进度失败",e);
        }
    }

    private void getConn() throws JobExecutionException{
        try {
            if(oldConn == null){
                String olddatasourceUrl = ParameterUtil.getOlddatasourceUrl();
                String olddatasourceUsername = ParameterUtil.getOlddatasourceUsername();
                String olddatasourcePassword = ParameterUtil.getOlddatasourcePassword();
                String olddatasourceDriver = ParameterUtil.getOlddatasourceDriver();
                String newdatasourceUrl = ParameterUtil.getNewdatasourceUrl();
                String newdatasourceUsername = ParameterUtil.getNewdatasourceUsername();
                String newdatasourcePassword = ParameterUtil.getNewdatasourcePassword();
                String newdatasourceDriver = ParameterUtil.getNewdatasourceDriver();
                if(olddatasourceDriver.equals(newdatasourceDriver)){
                    Class.forName(olddatasourceDriver);
                }
                oldConn = DriverManager.getConnection(olddatasourceUrl,olddatasourceUsername,olddatasourcePassword);
                newConn = DriverManager.getConnection(newdatasourceUrl,newdatasourceUsername,newdatasourcePassword);
            }
        } catch (ClassNotFoundException e) {
            throw new JobExecutionException("数据库连接初始化失败",e);
        } catch (SQLException e) {
            throw new JobExecutionException("数据库连接初始化失败",e);
        }

    }




}
