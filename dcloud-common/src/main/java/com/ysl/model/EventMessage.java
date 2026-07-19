package com.ysl.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventMessage implements Serializable {

    private String messageId;
    private String eventMessageType;
    private String bizId;
    private Long accountNo;
    private String content;
    private String remark;

}
