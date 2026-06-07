package com.ysl.component;

import com.ysl.util.CommonUtil;
import org.springframework.stereotype.Component;

@Component
public class ShortLinkComponent {

    private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public String createShortLinkCode(String param) {
        long murmurHash = CommonUtil.murmurHash( param);

        String code = encodeToBase62(murmurHash);
        return code;


    }

    private String encodeToBase62(long num) {
        StringBuffer sb = new StringBuffer();
        do {
            int i = (int)(num % 62);
            sb.append(CHARS.charAt(i));
            num = num / 62;
        }while (num > 0);

        String value = sb.reverse().toString();
        return value;
    }

}
