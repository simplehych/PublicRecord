package com.simple;

/**
 * 题目描述：二分查找变形
 * 把一个数组最开始的若干个元素搬到数组的末尾，我们称之为数组的旋转。
 * 输入一个 非递减排序数组（增或平？） 的一个旋转，输出旋转数组的最小元素。
 * 例如数组 {3,4,5,1,2} 为 {1,2,3,4,5} 的一个旋转，该数组的最小值为1。
 * NOTE：给出的所有元素都大于0，若数组大小为0，请返回0。
 * <p>
 * 思路：
 * 情况 1：非递减数组 {1, 2, 3, 4, 5}，旋转之后的一个可能 {3, 4, 5, 1, 2}
 * 情况 2：非递减数组 {0, 1, 1, 1, 1}，旋转之后的可能 A:{1, 0, 1, 1, 1}; B:{1, 1, 1, 0, 1};
 * 注意：处理数字相同时，指针移动的情况
 */
public class offer100_006 {

    public int bruteForce(int[] array) {
        if (array == null) return 0;

        int min = array[0];
        for (int i = 0; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }

    public int minNumberInRotateArray(int[] array) {
        return 0;
    }
}
