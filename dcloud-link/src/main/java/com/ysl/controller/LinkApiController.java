package com.ysl.controller;

import com.ysl.enums.ShortLinkStateEnum;
import com.ysl.service.ShortLinkService;
import com.ysl.util.CommonUtil;
import com.ysl.vo.ShortLinkVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Slf4j
@RequiredArgsConstructor
public class LinkApiController {

    private final ShortLinkService shortLinkService;


    public void dispatch(@PathVariable(name = "shortLinkCode") String shortLinkCode,
                         HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){

        try {
            log.info("断链码: {}", shortLinkCode);
            if(isLetterDigit(shortLinkCode)){
                ShortLinkVO shortLinkVO =shortLinkService.parseShortLink(shortLinkCode);
                if(isVisitable(shortLinkVO)){
                    httpServletResponse.setHeader("Location", shortLinkVO.getOriginalUrl());
                    httpServletResponse.setStatus(HttpStatus.FOUND.value());


                } else{
                    httpServletResponse.setStatus(HttpStatus.NOT_FOUND.value());
                    return;
                }
            }
        } catch (Exception e) {
            httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        }


    }
    private static boolean isVisitable(ShortLinkVO shortLinkVO) {
        if ((shortLinkVO != null && shortLinkVO.getExpired().getTime() > CommonUtil.getCurrentTimestamp())) {
            if (ShortLinkStateEnum.ACTIVE.name().equalsIgnoreCase(shortLinkVO.getState())) {
                return true;
            }
        } else if ((shortLinkVO != null && shortLinkVO.getExpired().getTime() == -1)) {
            if (ShortLinkStateEnum.ACTIVE.name().equalsIgnoreCase(shortLinkVO.getState())) {
                return true;
            }
        }

        return false;
    }

    private  static boolean isLetterDigit(String code){
        String regex = "^[a-zA-Z]+$";
        return code.matches(regex);
    }


}
