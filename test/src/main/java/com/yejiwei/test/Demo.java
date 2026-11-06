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

        Fu fu = new Fu();
        Zi zi = new Zi();
        System.out.println(fu.num);
        System.out.println(zi.num);
        fu.method();
        zi.method();
    }
}
