package com.ysl.component;

import com.google.common.hash.Hashing;
import com.ysl.util.CommonUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class ShortLinkTest {

    private static final Logger log = LoggerFactory.getLogger(ShortLinkTest.class);

    private final ShortLinkComponent shortLinkComponent = new ShortLinkComponent();

    @Test
    public void testMurmurHash() {
        for(int i=0; i<5; i++){

            String originalUrl = "https://xdclass.net?id="+ CommonUtil.generateUUID()+"pwd="+CommonUtil.getStringNumRandom(7);

            long murmur3_32 = Hashing.murmur3_32().hashUnencodedChars(originalUrl).padToLong();

            log.info("murmur3_32={}",murmur3_32);

        }
    }
    @Test
    public void testCreateShortLinkCode() {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int num1 = random.nextInt(10);
            int num2 = random.nextInt(10000000);
            int num3 = random.nextInt(10000000);
            String originalUrl = num1 + "xdclass" + num2 + ".net" + num3;
            String shortLinkCode = shortLinkComponent.createShortLinkCode(originalUrl);
            log.info("originalUrl:" + originalUrl + ", shortLinkCode=" + shortLinkCode);
        }
    }

}
