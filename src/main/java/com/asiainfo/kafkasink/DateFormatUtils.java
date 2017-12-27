package com.asiainfo.kafkasink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Timestamp;


/**
 * Created by yang on 2017/12/18.
 */
public class DateFormatUtils {
    private static final Logger logger = LoggerFactory.getLogger(DateFormatUtils.class);

    /**字符串转换为时间戳
     * dateStr格式必须是yyyy-[m]m-[d]d hh:mm:ss[.f...]格式
     * */
    public static Timestamp dateString2Timestamp(String dateStr){
        Timestamp result=new Timestamp(new Date().getTime());

        if(dateStr!=null && !"".equals(dateStr)){
            try {
                result = Timestamp.valueOf(dateStr.trim());
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
        return result;
    }

    public static Timestamp dateString2Timestamp(String dateStr,String formate){
        Timestamp result=new Timestamp(new Date().getTime());

        if(dateStr!=null && !"".equals(dateStr)){
            try {
                SimpleDateFormat df =new SimpleDateFormat(formate);
                result=new Timestamp(df.parse(dateStr.trim()).getTime());
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
        return result;
    }
//    public static void main(String args[]){
//        String str="2015-12-2 9:2:3";
//        Timestamp t=DateFormatUtils.dateString2Timestamp(str);
//        System.out.println(t);
//        System.out.println(t.getTime());
//    }
}
