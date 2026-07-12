package biz;

import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.ysl.AccountApplication;
import com.ysl.mapper.TrafficMapper;
import com.ysl.model.TrafficDO;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AccountApplication.class)
@Slf4j
public class  TrafficTest {
    private TrafficMapper trafficMapper;

    @Autowired
    public void setTrafficMapper(TrafficMapper trafficMapper) {
        this.trafficMapper = trafficMapper;
    }

    @Test
    public void testSaveTraffic() {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            TrafficDO trafficDO = new TrafficDO();
            trafficDO.setAccountNo(Long.valueOf(random.nextInt(100)));
            trafficMapper.insert(trafficDO);
        }
        log.info("testMapper:{}", trafficMapper);
    }
}
