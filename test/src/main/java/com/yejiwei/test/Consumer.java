package com.yejiwei.test;

/**
 * 消费者
 */
public class Consumer implements Runnable {
    private BaoZiPu baoZiPu;
    public Consumer(BaoZiPu baoZiPu) {
        this.baoZiPu = baoZiPu;
    }
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (baoZiPu) {
                //1.如果flag为false,证明没有包子,消费线程等待
                if (baoZiPu.isFlag() == false) {
                    try {
                        baoZiPu.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //2.如果flag为true,证明有包子,就要消费包子
                baoZiPu.getCount();
                //3.修改flag状态为false,证明没有包子了
                baoZiPu.setFlag(false);
                //4.唤醒生产线程
                baoZiPu.notify();
                System.out.println("消费者线程退出synchronized");
            }
        }
    }
}
