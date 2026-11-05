package com.yejiwei.test;

import java.util.Arrays;

public class Demo {
    static void change(int [] arr) {
        arr[0] = 1;
    }
    public static void main(String[] args) {
        System.out.println("Hello World!");
        int arr[] = {0};
        System.out.println(Arrays.toString(arr));
        change(arr);
        System.out.println(Arrays.toString(arr));
    }
}
