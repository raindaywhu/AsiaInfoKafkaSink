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
    private String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private SimpleDateFormat df = null;

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


//    val threadLocalSDF = new ThreadLocal[SimpleDateFormat]{
//        override def initialValue() = new SimpleDateFormat()
//    }
//    public DateFormatUtils(){
//        this.df=new SimpleDateFormat(DEFAULT_PATTERN);
//    }
//    public DateFormatUtils(String format){
//        if(format==null ||"".equals(format)){
//            format=this.DEFAULT_PATTERN;
//        }
//        this.df=new SimpleDateFormat(format);
//    }
//
//    //毫秒转换为字符串
//    public String timestamp2Str(Long timestamp){
//        if(timestamp==0)timestamp=new Date().getTime();
//        //SimpleDateFormat defaultSDF = new SimpleDateFormat(DEFAULT_PATTERN);
//        return df.format(new Date(timestamp));
//    }
//
//    //字符串转换为毫秒
//    public Long dateString2Timestamp(String dateStr){
//        Timestamp t= new Timestamp(new Date().getTime());
//        t.valueOf(dateStr);
//        Timestamp a=Timestamp.valueOf(dateStr);
//        if(dateStr==null || "".equals(dateStr) || dateStr.length()<10 ){
//            return new Date().getTime();
//        }else{
//            try{
//                return df.parse(dateStr.trim()).getTime();
//            }catch(Exception e){
//                logger.error("trans "+dateStr+" to timestamp ERROR: "+e.getMessage());
//
//            }
//        }
//        //SimpleDateFormat defaultSDF = new SimpleDateFormat(DEFAULT_PATTERN);
//        return 0L;
//    }


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
