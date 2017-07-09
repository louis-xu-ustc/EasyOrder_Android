package edu.cmu.EasyOrder_Android;

/**
 * Created by yunpengx on 7/9/17.
 */

public class Order {
    public String userName;
    public Boolean ifNotify;

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getIfNofity() {
        return this.ifNotify;
    }

    public void setIfNotify(Boolean ifNotify) {
        this.ifNotify = ifNotify;
    }
}
