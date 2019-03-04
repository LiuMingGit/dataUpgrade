package com.chis.util;
/**
 * 日期：2019年03月01日
 * 作者：刘铭
 * 邮箱：liuming@bsoft.com.cn
 */

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * yaml配置文件读取类
 */
public class YamlReader {
    /**
     * yaml配置全局缓存
     */
    private static HashMap<String,HashMap<String,Object>> properties;

    private YamlReader(){
        if(SingletonHolder.instance != null){
            throw new IllegalStateException("yaml读取器初始化异常");
        }
    }

    /**
     * 初始化
     */
    static {
        InputStream inputStream = null;
        try {
            properties = new HashMap<>();
            Yaml yaml = new Yaml();
            inputStream = YamlReader.class.getClassLoader().getResourceAsStream("oracle.yaml");
            properties = yaml.loadAs(inputStream,HashMap.class);
        }catch (Exception e){
            throw new IllegalStateException("yaml读取器初始化异常");
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new IllegalStateException("yaml读取器初始化异常");
            }
        }
    }

    /**
     * 单例实现
     */
    private static class SingletonHolder{
        private static YamlReader instance = new YamlReader();
    }

    /**
     * 获取Yaml实例
     * @return
     */
    public static YamlReader getInstance(){
        return SingletonHolder.instance;
    }

    public Object getValuebyKey(String root, String key){
        Map<String,Object> rootProperty = properties.get(root);
        return rootProperty.get(key);
    }
}
