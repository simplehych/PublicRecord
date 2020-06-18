package com.simple;


/**
 * 题目描述：
 * 大家都知道斐波那契数列，现在要求输入一个整数n，请你输出斐波那契数列的第n项（从0开始，第0项为0，第1项是1）。
 * n<=39
 */
public class offer100_007 {

    public int Fibonacci(int n) {
        return 0;
    }

    public int bruteForce(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;

        int k1 = 0;
        int k2 = 1;

        int index = 2;
        int value = 1;

        while (index <= n) {
            value = k1 + k2;
            k1 = k2;
            k2 = value;
            index++;
        }
        return value;
    }
}
