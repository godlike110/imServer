package com.easychat.support;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Zed
 * date: 2019/08/19.
 * description:
 */
@Data
public abstract class Packet implements Serializable {
    private transient byte version = 1;
    /** 消息时间*/
    protected String dateTime;

    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public abstract byte getCommand();
}
