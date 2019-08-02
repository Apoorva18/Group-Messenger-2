package edu.buffalo.cse.cse486586.groupmessenger2;

import java.io.Serializable;

public class InitialMessage implements Serializable {
    String id;
    String msg;
    public  InitialMessage(String id,String msg){
        this.id = id;
        this.msg= msg;
    }

    public String getId() {
        return id;
    }

    public String getMsg() {
        return msg;
    }
}
