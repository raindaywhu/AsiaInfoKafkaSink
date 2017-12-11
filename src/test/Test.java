import com.asiainfo.kafkasink.*;
import org.apache.commons.lang.StringUtils;

/**
 * Created by yang on 2017/12/8.
 */
public class Test {
    public static void main(String args[]){
        String posi2g="start_time,1,460000001344554443,imei,tmsi_o,start_lac,start_ci,end_lac,end_ci,msisdn";
        String call2g="start_time,1,calling,called,460000001344554443,imei,tmsi_o,start_lac,start_ci,end_lac,end_ci";
        String sms2g="start_time,0,calling,460000001344554443,imei,tmsi,start_lac,start_ci,called";
        String posi3g="start_time,0,460000001344554443,IMEI,TMSI,MCC,MNC,LAC,SAC,oldLAC,oldSAC";
        String call3g="start_time,0,calling,called,460000001344554443,IMEI,TMSI,LAC,SAC,endLAC,endSAC";
        String sms3g="start_time,1,calling,called,460000001344554443,IMEI,TMSI,LAC,SAC";

        String result21=new YunNan2GMessagePreprocessor().transformMessage(posi2g);
        String result22=new YunNan2GCallPreprocessor().transformMessage(call2g);
        String result23=new YunNan2GSmsPreprocessor().transformMessage(sms2g);
        String result31=new YunNan3GMessagePreprocessor().transformMessage(posi3g);
        String result32=new YunNan3GCallPreprocessor().transformMessage(call3g);
        String result33=new YunNan3GSmsPreprocessor().transformMessage(sms3g);
        System.out.println(result21);
        System.out.println(result22);
        System.out.println(result23);
        System.out.println(result31);
        System.out.println(result32);
        System.out.println(result33);
    }
}
