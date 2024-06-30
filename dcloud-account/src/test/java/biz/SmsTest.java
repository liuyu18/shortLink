package biz;

import com.ysl.AccountApplication;
import com.ysl.component.SmsComponent;
import com.ysl.config.SmsConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AccountApplication.class)
@Slf4j
public class SmsTest {
    @Autowired
    private SmsComponent smsComponent;
    @Autowired
    private SmsConfig smsConfig;
    
    @Test
    public void testSendSms() {
        smsComponent.send("18819165057", smsConfig.getTemplateId(), "666666");
    }
}