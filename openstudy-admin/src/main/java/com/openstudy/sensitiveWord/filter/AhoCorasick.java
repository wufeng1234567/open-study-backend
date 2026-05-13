package com.openstudy.sensitiveWord.filter;

import java.util.*;

/**
 * AC自动机（Aho-Corasick）实现
 * 用于高效的多模式字符串匹配
 */
public class AhoCorasick {

    private final Node root = new Node();
    private boolean built = false;

    /**
     * 插入一个模式串
     */
    public void insert(String word) {
        if (word == null || word.isEmpty()) return;
        Node node = root;
        for (char c : word.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new Node());
        }
        node.isEnd = true;
        node.word = word;
        node.length = word.length();
    }

    /**
     * 构建失败指针（必须在所有insert之后调用）
     */
    public void buildFailureLinks() {
        Queue<Node> queue = new LinkedList<>();
        // 第一层节点的失败指针指向root
        for (Node child : root.children.values()) {
            child.fail = root;
            queue.offer(child);
        }
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            for (Map.Entry<Character, Node> entry : current.children.entrySet()) {
                char c = entry.getKey();
                Node child = entry.getValue();
                queue.offer(child);
                // 找到child的失败指针
                Node failNode = current.fail;
                while (failNode != null && !failNode.children.containsKey(c)) {
                    failNode = failNode.fail;
                }
                child.fail = failNode == null ? root : failNode.children.get(c);
            }
        }
        built = true;
    }

    /**
     * 检查文本是否包含任意敏感词
     */
    public boolean contains(String text) {
        if (text == null || text.isEmpty()) return false;
        if (!built) throw new IllegalStateException("请先调用 buildFailureLinks()");
        Node node = root;
        for (char c : text.toCharArray()) {
            while (node != root && !node.children.containsKey(c)) {
                node = node.fail;
            }
            if (node.children.containsKey(c)) {
                node = node.children.get(c);
            }
            if (node.isEnd) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找文本中第一个敏感词
     */
    public String findFirst(String text) {
        if (text == null || text.isEmpty()) return null;
        if (!built) throw new IllegalStateException("请先调用 buildFailureLinks()");
        Node node = root;
        for (char c : text.toCharArray()) {
            while (node != root && !node.children.containsKey(c)) {
                node = node.fail;
            }
            if (node.children.containsKey(c)) {
                node = node.children.get(c);
            }
            if (node.isEnd) {
                return node.word;
            }
        }
        return null;
    }

    /**
     * 查找文本中所有敏感词（不重叠）
     */
    public List<String> findAll(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) return result;
        if (!built) throw new IllegalStateException("请先调用 buildFailureLinks()");
        Node node = root;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            while (node != root && !node.children.containsKey(c)) {
                node = node.fail;
            }
            if (node.children.containsKey(c)) {
                node = node.children.get(c);
            }
            if (node.isEnd) {
                result.add(node.word);
                // 继续匹配，不跳过
            }
        }
        return result;
    }

    /**
     * 替换文本中的敏感词为指定字符
     */
    public String replace(String text, char replaceChar) {
        if (text == null || text.isEmpty()) return text;
        if (!built) throw new IllegalStateException("请先调用 buildFailureLinks()");
        char[] chars = text.toCharArray();
        Node node = root;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            while (node != root && !node.children.containsKey(c)) {
                node = node.fail;
            }
            if (node.children.containsKey(c)) {
                node = node.children.get(c);
            }
            if (node.isEnd) {
                // 替换匹配到的词
                int start = i - node.length + 1;
                for (int j = start; j <= i; j++) {
                    chars[j] = replaceChar;
                }
            }
        }
        return new String(chars);
    }

    /**
     * 获取当前词库大小
     */
    public int size() {
        return countNodes(root) - 1; // 减去root
    }

    private int countNodes(Node node) {
        int count = 1;
        for (Node child : node.children.values()) {
            count += countNodes(child);
        }
        return count;
    }

    // 内部节点类
    private static class Node {
        Map<Character, Node> children = new HashMap<>();
        Node fail;
        boolean isEnd;
        String word;
        int length;
    }
}