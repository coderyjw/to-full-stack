package com.yejiwei.test;

/**
 * 生产者
 */
public class Product implements Runnable {
    private BaoZiPu baoZiPu;
    public Product(BaoZiPu baoZiPu) {
        this.baoZiPu = baoZiPu;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (baoZiPu) {
                //1.如果flag为true,证明有包子,生产线程等待
                if (baoZiPu.isFlag() == true) {
                    try {
                        baoZiPu.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //2.如果flag为false,证明没有包子,就要生产包子
                baoZiPu.setCount();
                //3.修改flag状态为true,证明有包子了
                baoZiPu.setFlag(true);
                //4.唤醒消费线程
                baoZiPu.notify();
                System.out.println("生产者线程退出synchronized");
            }
        }
    }
}