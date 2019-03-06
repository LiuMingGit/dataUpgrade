package com.chis.pojo;


/**
 * 日期：2019年03月05日
 * 作者：刘铭
 * 邮箱：liuming@bsoft.com.cn
 */


/**
 * 数据抽取进行进度
 */
public class Progresz {

    /**
     * 数据抽取表
     */
    private  String tableName;
    /**
     * 总数
     */
    private  Long total;
    /**
     * 已传输数
     */
    private  Long transportNumber;
    /**
     * 进度
     */
    private  String progress;


    public Progresz(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getTransportNumber() {
        return transportNumber;
    }

    public void setTransportNumber(Long transportNumber) {
        this.transportNumber = transportNumber;
    }

    public String getProgress() {
        Long totalnum = total;
        Long transport = transportNumber;
        if(totalnum == null)
            totalnum = 0L;
        if(transport == null)
            transport= 0L;
        if(totalnum == 0L){
            setTransportNumber(0L);
            return "100%";
        }
        return transport/totalnum*100+"%";
    }


    @Override
    public String toString() {
        return "Progresz{" +
                "tableName='" + tableName + '\'' +
                ", total=" + total +
                ", transportNumber=" + transportNumber +
                ", progress=" + getProgress() +
                '}';
    }
}
