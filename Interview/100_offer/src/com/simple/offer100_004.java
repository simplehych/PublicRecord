package com.simple;

import java.util.Arrays;

/**
 * 题目描述：
 * 输入某二叉树的前序遍历和中序遍历的结果，请重建出该二叉树。
 * 假设输入的前序遍历和中序遍历的结果中都不含重复的数字。
 * 例如输入前序遍历序列{1,2,4,7,3,5,6,8}和中序遍历序列{4,7,2,1,5,3,8,6}，则重建二叉树并返回。
 * <p>
 * 思路：
 * 1. 区别前中后序遍历（以“根”的位置记忆）
 * 前序遍历：‘根’左右
 * 中序遍历：左‘根’右
 * 后序遍历：左右‘根’
 * 2. 前序：第 1 个为根节点
 * 3. 中序：根节点左边为左子树，根节点右边为右子树
 * 4. 前序+中序：唯一确定树；后序+中序：唯一确定树；前序+后序：不唯一确定树；
 */
public class offer100_004 {

    public class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int x) {
            val = x;
        }

        @Override
        public String toString() {
            return "TreeNode{" +
                    "val=" + val +
                    ", left=" + left +
                    ", right=" + right +
                    '}';
        }
    }

    public TreeNode reConstructBinaryTree(int[] pre, int[] in) {
        if (pre.length == 0 || in.length == 0) {
            return null;
        }

        TreeNode result = new TreeNode(pre[0]);

        int inRoot = -1;
        for (int i = 0; i < in.length; i++) {
            if (in[i] == pre[0]) {
                inRoot = i;
                break;
            }
        }

        // 左闭右开 [  )
        int[] leftIn = Arrays.copyOfRange(in, 0, inRoot);
        int[] rightIn = Arrays.copyOfRange(in, inRoot + 1, in.length);

        int[] leftPre = Arrays.copyOfRange(pre, 1, 1 + leftIn.length);
        int[] rightPre = Arrays.copyOfRange(pre, 1 + leftIn.length, pre.length);

        result.left = reConstructBinaryTree(leftPre, leftIn);
        result.right = reConstructBinaryTree(rightPre, rightIn);

        return result;
    }

    public static void main(String[] args) {
        int[] pre = {1, 2, 4, 7, 3, 5, 6, 8};
        int[] in = {4, 7, 2, 1, 5, 3, 8, 6};

        offer100_004 offer100_004 = new offer100_004();
        TreeNode treeNode = offer100_004.reConstructBinaryTree(pre, in);
        System.out.print(treeNode.toString());
    }
}
