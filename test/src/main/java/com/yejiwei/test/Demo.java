package com.yejiwei.test;

import java.util.Arrays;

public class Demo {
    static void change(int [] arr) {
        arr[0] = 1;
    }
    public static void main(String[] args) {
//        System.out.println("Hello World!");
//        int arr[] = {0};
//        System.out.println(Arrays.toString(arr));
//        change(arr);
//        System.out.println(Arrays.toString(arr));

//        Person person = new Person();
//        System.out.println(person+"...");
//        person.name = "沉香";
//        person.speak("刘彦昌");
//
//        System.out.println("====================");
//
//        Person person2 = new Person();
//        System.out.println(person2+"......");
//        person2.name = "王思聪";
//        person2.speak("王健林");
//
//        Fu fu = new Fu();
//        Zi zi = new Zi();
//        System.out.println(fu.num);
//        System.out.println(zi.num);
//        fu.method();
//        zi.method();

//        State weifukuan = State.WEIFUKUAN;
//        System.out.println("weifukuan = " + weifukuan); // weifukuan = WEIFUKUAN
//
//        State weifahuo = State.WEIFAHUO;
//        System.out.println(weifahuo.getName()); // 未发货
//
//        System.out.println("=============================");
//        State yifahuo = State.YIFAHUO;
//        System.out.println(yifahuo); // YIFAHUO
//        System.out.println(yifahuo.toString()); // YIFAHUO
//        System.out.println("=============================");
//        State[] values = State.values();
//        for (int i = 0; i < values.length; i++) {
//            System.out.println(values[i].getName());
//        }
//        System.out.println("============================");
//        State yifahuo1 = State.valueOf("YIFAHUO");
//        System.out.println("yifahuo1 = " + yifahuo1); // yifahuo1 = YIFAHUO

        BaoZiPu baoZiPu = new BaoZiPu();

        Product product = new Product(baoZiPu);
        Consumer consumer = new Consumer(baoZiPu);

        Thread t1 = new Thread(product);
        Thread t2 = new Thread(consumer);

        t1.start();
        t2.start();
    }
}
