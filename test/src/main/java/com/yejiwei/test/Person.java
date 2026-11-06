package com.yejiwei.test;

public class Person {
    String name;
    public void speak(String name){
        System.out.println(this+"...............");
        System.out.println(this.name+"您好,我是"+name);
    }
}