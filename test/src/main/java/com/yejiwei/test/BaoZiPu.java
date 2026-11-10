package com.yejiwei.test;

public class BaoZiPu {
    private int count;
    private boolean flag;

    public BaoZiPu() {
    }

    public BaoZiPu(int count, boolean flag) {
        this.count = count;
        this.flag = flag;
    }

    /**
     * get方法专门为消费线程服务
     */
    public void getCount() {
        System.out.println("消费了第......"+count+"个包子");
    }

    /**
     * set方法专门为生产线程服务
     */
    public void setCount() {
        count++;
        System.out.println("生产了第..."+count+"个包子");
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}