package com.simple;

import java.util.Stack;

/**
 * 题目描述：
 * 用两个栈来实现一个队列，完成队列的Push和Pop操作。 队列中的元素为int类型。
 */
public class offer100_005 {

    Stack<Integer> stack1 = new Stack<Integer>();
    Stack<Integer> stack2 = new Stack<Integer>();

    public void push(int node) {
        stack1.push(node);
    }

    public int pop() {
        if (!stack2.isEmpty()) {
            return stack2.pop();
        }
        if (stack1.isEmpty()) {
            return -1;
        }
        while (stack1.size() != 0) {
            stack2.push(stack1.pop());
        }
        // 错误示范：使用循环应该出栈
        /*for (Integer next : stack1) {
            stack2.push(next);
        }*/
        return stack2.pop();
    }
}
