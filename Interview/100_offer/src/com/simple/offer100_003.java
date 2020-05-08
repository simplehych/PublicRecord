package com.simple;

import java.util.ArrayList;

class offer100_003 {

    public static class ListNode {
        int val;
        ListNode next = null;

        ListNode(int val) {
            this.val = val;
        }
    }

    public ArrayList<Integer> printListFromTailToHead(ListNode listNode) {
        ArrayList<Integer> result = new ArrayList<>();

        if (listNode == null)
            return result;

        result.add(listNode.val);

        ListNode next = listNode.next;
        while (next != null) {
            result.add(0, next.val);
            next = next.next;
        }
        return result;
    }

    public static void main(String[] args) {
        ListNode listNode = new ListNode(2);
        listNode.next = new ListNode(1);
        ArrayList<Integer> result = new offer100_003().printListFromTailToHead(listNode);
        System.out.print(result);
    }
}
