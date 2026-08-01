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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Slf4j
@RequiredArgsConstructor
public class LinkApiController {

    private final ShortLinkService shortLinkService;


    @GetMapping(path = "/{shortLinkCode}")
    public void dispatch(
            @PathVariable(name = "shortLinkCode") String shortLinkCode,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {

        try {
            log.info("断链码: {}", shortLinkCode);
            if (isLetterDigit(shortLinkCode)) {
                ShortLinkVO shortLinkVO = shortLinkService.parseShortLinkCode(shortLinkCode);
                if (isVisitable(shortLinkVO)) {
                    String originalUrl = CommonUtil.removeUrlPrefix(shortLinkVO.getOriginalUrl());
                    httpServletResponse.setHeader("Location", originalUrl);
                    httpServletResponse.setStatus(HttpStatus.FOUND.value());


                } else {
                    httpServletResponse.setStatus(HttpStatus.NOT_FOUND.value());
                }
            }
        } catch (Exception e) {
            httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        }


    }

    private static boolean isVisitable(ShortLinkVO shortLinkVO) {
        if ((shortLinkVO != null && shortLinkVO.getExpired().getTime() > CommonUtil.getCurrentTimestamp())) {
            return ShortLinkStateEnum.ACTIVE.name().equalsIgnoreCase(shortLinkVO.getState());
        } else if ((shortLinkVO != null && shortLinkVO.getExpired().getTime() == -1)) {
            return ShortLinkStateEnum.ACTIVE.name().equalsIgnoreCase(shortLinkVO.getState());
        }

        return false;
    }

    private static boolean isLetterDigit(String code) {
        String regex = "^[a-zA-Z]+$";
        return code.matches(regex);
    }


}
