package com.asiainfo.kafkasink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yang on 2017/12/18.
 */
public class DateFormatUtils {
    private static final Logger logger = LoggerFactory.getLogger(DateFormatUtils.class);
    private String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private SimpleDateFormat defaultSDF = null;

//    val threadLocalSDF = new ThreadLocal[SimpleDateFormat]{
//        override def initialValue() = new SimpleDateFormat()
//    }
    public DateFormatUtils(){
        this.defaultSDF=new SimpleDateFormat(DEFAULT_PATTERN);
    }
    public DateFormatUtils(String format){
        if(format==null ||"".equals(format)){
            format=this.DEFAULT_PATTERN;
        }
        this.defaultSDF=new SimpleDateFormat(format);
    }

    //毫秒转换为字符串
    public String timestamp2Str(Long timestamp){
        if(timestamp==0)timestamp=new Date().getTime();
        //SimpleDateFormat defaultSDF = new SimpleDateFormat(DEFAULT_PATTERN);
        return defaultSDF.format(new Date(timestamp));
    }

    //字符串转换为毫秒
    public Long dateString2Timestamp(String dateStr){
        //SimpleDateFormat defaultSDF = new SimpleDateFormat(DEFAULT_PATTERN);
        try{
            return defaultSDF.parse(dateStr).getTime();
        }catch(Exception e){
            logger.error(e.getMessage());

        }
        return 0L;

    }


//    public String  dateMs2Str(Long dateMs, String pattern){
//        threadLocalSDF.get().applyPattern(pattern);
//        return threadLocalSDF.get().format(new Date(dateMs));
//    }
//
//    def dateStr2Ms(dateStr: String, pattern: String): Long ={
//        threadLocalSDF.get().applyPattern(pattern)
//        threadLocalSDF.get().parse(dateStr).getTime
//    }
}
