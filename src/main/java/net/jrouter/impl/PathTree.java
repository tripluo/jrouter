/*
 * Copyright (C) 2010-2111 sunjumper@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.jrouter.impl;

import java.io.Serializable;
import java.util.*;

/**
 * 适配{@code Map}接口的树结构路径。包含了一个树结构路径和一个含相关联值的最终路径的{@code Set}集合。
 *
 * @param <V> 与路径相关联值的类型。
 */
class PathTreeMap<V> extends AbstractMap<String, V> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 树结构路径 */
    private PathTree<V> tree;

    /** entrySet views for adapting for the Map interface */
    private transient Set<Map.Entry<String, V>> entrySet = null;

    /**
     * 构造一个指定路径分割符的映射路径和关联值的映射。
     *
     * @param separator 指定的路径分割符。
     */
    public PathTreeMap(char separator) {
        super();
        tree = new PathTree<>(separator);
        entrySet = new HashSet<>();
    }

    /**
     * 添加指定的全路径与其相关联的值至视图。
     *
     * @param fullPath 指定的全路径。
     * @param value 与路径相关联的值。
     */
    private void addEntrySet(String fullPath, V value) {
        entrySet.add(new SimpleImmutableEntry<>(fullPath, value));
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V put(String fullPath, V value) {
        V res = tree.put(fullPath, value);
        // if add new to addEntrySet
        if (res == null) {
            addEntrySet(fullPath, value);
        }
        return res;
    }

    /**
     * @see PathTree#get(String, Map)
     */
    public V get(String fullPath, Map<String, String> matchParameters) {
        return tree.get(fullPath, matchParameters);
    }

    @Override
    public V get(Object fullPath) {
        return tree.get(fullPath.toString(), null);
    }

    @Override
    public void clear() {
        entrySet.clear();
        tree.clear();
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        return entrySet;
    }
}

/**
 * 树形结构存储的映射路径与其关联值。
 *
 * @param <V> 与路径相关联值的类型。
 */
class PathTree<V> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 默认的路径分隔符 */
    public static final char PATH_SEPARATOR = '/';

    /** 路径分割符 */
    private char pathSeparator = PATH_SEPARATOR;

    /** 单路径匹配标识 */
    public static final String SINGLE_MATCH = "*";

    /**
     * 路径尾匹配
     *
     * @deprecated
     */
    @Deprecated
    private static final String LAST_MATCH = "**";

    /** 树路径的根节点 */
    private TreeNode<V> root;

    /**
     * 构造一个默认路径分割符'/'的路径树。
     */
    PathTree() {
        this(PATH_SEPARATOR);
    }

    /**
     * 构造一个指定路径分割符的路径树。
     *
     * @param pathSeparator 指定的路径分割符。
     */
    PathTree(char pathSeparator) {
        this.pathSeparator = pathSeparator;
        root = new TreeNode<>(Character.toString(pathSeparator), null);
        root.code = 1;
    }

    /**
     * 判断是否为根路径。
     *
     * @param fullPath 指定的全路径。
     *
     * @return 是否为根路径。
     */
    private boolean isRoot(String fullPath) {
        return fullPath.length() == 1 && pathSeparator == fullPath.charAt(0);
    }

    /**
     * 添加路径与其相关联的值，并返回原有路径的值；
     * 如果原有路径已存在值则保留并返回该值，不存在则返回 null。
     *
     * @param fullPath 指定的相关路径。
     * @param value 与路径相关联的值。
     *
     * @return 以前与路径相关联的值，如果没有则返回 null。
     *
     * @throws NullPointerException 如果路径相关联的值为 null。
     */
    public V put(String fullPath, V value) {
        if (value == null) {
            throw new NullPointerException();// NOPMD
        }
        // root path
        if (isRoot(fullPath)) {
            V oldRoot = root.value;
            root.value = value;
            return oldRoot;
        }

        final String[] paths = parsePath(fullPath);
        int len = paths.length;
        if (len == 0) {
            throw new IllegalArgumentException("Null path : " + fullPath);
        }
        TreeNode<V> cur = root;
        for (int i = 0; i < len - 1; i++) {
            // add tree branches
            cur = cur.addBranch(paths, paths[i]);
//            cur = cur.get(paths[i]);
        }
        // add the last path with value.
        // return null means to add a new node, else return old node with value.
        return cur.addLeaf(paths, paths[len - 1], value);
    }

    /**
     * 获取指定路径相关联的值；如果不包含该路径的关联关系，则返回 null。
     *
     * @param fullPath 指定路径的名称。
     *
     * @return 指定路径相关联的值；如果不包含该路径的关联关系，则返回 null。
     */
    public V get(String fullPath) {
        return get(fullPath, null);
    }

    /**
     * 获取指定路径相关联的值；如果不包含该路径的关联关系，则返回 null。
     *
     * @param fullPath 指定路径的名称。
     * @param matchParameters 路径中匹配的键值映射。
     *
     * @return 指定路径相关联的值；如果不包含该路径的关联关系，则返回 null。
     */
    public V get(String fullPath, Map<String, String> matchParameters) {
        // root path
        if (isRoot(fullPath)) {
            return root.value;
        }
        String[] paths = parsePath(fullPath);

        int len = paths.length;
        if (len == 0) {
            return null;
        }
        // the current nodes as parents
        List<TreeNode<V>> current = new ArrayList<>(1);
        // the next all children nodes
        List<TreeNode<V>> next = new ArrayList<>(5);

        // 初始化当前的节点集合指向根节点
        current.add(root);

        // 遍历树
        for (int i = 0; i < len - 1; i++) {
            for (TreeNode<V> tn : current) {
                if (tn.children == null || tn.children.isEmpty()) {
                    continue;
                }
                TreeNode<V> match = tn.children.get(paths[i]);
                if (match != null) {
                    next.add(match);
                }
                match = tn.children.get(SINGLE_MATCH);
                if (match != null) {
                    next.add(match);
                }
            }
//            System.out.println("Next : " + next + ", Current : " + current);
            if (next.isEmpty()) {
//                System.out.println("Not Found For [" + fullPath + "]");
                // not find
                return null;
            }

            // change the current to the next then clear the next for reuse
            List<TreeNode<V>> temp = current;
            current = next;
            next = temp;
            next.clear();
        }

        // 找寻最终路径有值的节点
        for (TreeNode<V> tn : current) {
            if (tn.children == null || tn.children.isEmpty()) {
                continue;
            }
            TreeNode<V> match = tn.children.get(paths[len - 1]);
            if (match != null && match.value != null) {
                next.add(match);
            }
            match = tn.children.get(SINGLE_MATCH);
            if (match != null && match.value != null) {
                next.add(match);
            }
        }
        if (next.isEmpty()) {
//            System.out.println("Not Found For [" + fullPath + "]");
            // not find
            return null;
        }
        current.clear();
        current = next;

//        System.out.println("Final Match Nodes List : " + current);
        // 最终匹配的路径节点
        TreeNode<V> finalMatcher = null;

        // the final list must have at least on value
        if (current.size() == 1) {
            finalMatcher = current.get(0);
            fillMatchParameters(finalMatcher, paths, matchParameters);
            return finalMatcher.value;
        }

        // compare the paths to find the most matched one which has the maximum code.
        int max = current.get(0).code;
        int index = 0;
        int size = current.size();
        for (int i = 1; i < size; i++) {
            TreeNode<V> tr = current.get(i);
            if (tr.code > max) {
                max = tr.code;
                index = i;
            }
        }
//        System.out.println("Index & Max : " + index + "," + max);

        // get the final match node
        finalMatcher = current.get(index);
        current.clear();

        // fill the MatchParameters
        fillMatchParameters(finalMatcher, paths, matchParameters);
        return finalMatcher.value;
    }

    /*
     * 将路径数组按照指定的路径码（二进制标识）填充进链表。
     */
    private void fillMatchParameters(TreeNode<V> matcher, String[] paths, Map<String, String> matchParameters) {
        if (matchParameters != null) {
            /**
             * //parse the * parameters
             * String fullCode = Integer.toBinaryString(matcher.code);
             * //0 is the root '/'
             * for (int i = 1; i < fullCode.length(); i++) {
             * //if 0 means '*'
             * if (fullCode.charAt(i) == '0') {
             * matchParameters.put(i - 1 + "", paths[i - 1]);
             * }
             * }
             *
             */

            IndexKey[] iks = matcher.indexKeys;
            if (iks != null) {
                for (IndexKey ik : iks) {
                    // 不做索引下标映射
                    // matchParameters.put(i + "", paths[ik.index]);
                    // 不做匹配路径键值的重复判断
                    matchParameters.put(ik.matchKey, paths[ik.index]);
                }
            }
        }
//        System.out.println("Get Final matchParameters : " + matchParameters);
    }

    /**
     * 将全路径解析成字符串数组，排除了"连续分割符"。
     *
     * @param fullPath 全路径字符串。
     *
     * @return 解析后的事字符串数组。
     */
    private String[] parsePath(String fullPath) {
        List<String> list = new ArrayList<>(5);
        // fullPath is trimmed
        int last = -1;
        int cur = -1;
        int len = fullPath.length();
        for (int i = 0; i < len; i++) {
            if (pathSeparator == fullPath.charAt(i)) {
                cur = i;
                if (cur > last) {
                    // avoid ...///...
                    if (cur - last > 1) {
                        list.add(fullPath.substring(last + 1, cur));
                    }
                    last = cur;
                }
            }
        }
        if (cur < len - 1) {
            cur = len;
            list.add(fullPath.substring(last + 1, cur));
        }
        return list.toArray(new String[0]);
    }

    /**
     * 清除整个树结构的所有路径与其相关联值的映射关系。
     */
    public void clear() {
        root = null;
    }

    /**
     * 获取添加路径的非完全匹配名称，完全字符串则返回 null。
     * <p>
     * --> null
     * * --> null
     * abc --> null
     * {} --> null
     * {abc} --> abc
     * { abc } --> abc (" abc ")
     * {*} --> *
     * { } --> (" ")
     * {{abc} --> {abc
     * {{abc}}} --> {abc}}
     *
     * @param path 路径字符串。
     *
     * @return 路径的非完全匹配名称，完全字符串则返回 null。
     */
    private static String getMatchKey(String path) {
        int len = path.length();

        // at least 2 chars
        if (len <= 2) {
            return null;
        }
        int begin = -1;
        int end = -1;
        for (int i = 0; i < len; i++) {
            char c = path.charAt(i);
            if (begin == -1 && (c == '{' || c == '[' || c == '(')) {
                begin = i;
            } else if (c == '}' || c == ']' || c == ')') {
                end = i;
            }
        }
        // return the no trimmed string between the first '{[(' and the last '}])'.
        return begin != -1 && end - begin > 1 ? path.substring(begin + 1, end) : null;
    }

    /**
     * 判断是否是一个键匹配的路径。
     *
     * @param path 指定路径。
     *
     * @return 路径包含键匹配返回true，否则返回false。
     *
     * @see #getMatchKey(java.lang.String)
     */
    private static boolean isMatchKay(String path) {
        int len = path.length();
        // at least 2 chars
        if (len <= 2) {
            return false;
        }
        int begin = -1;
        int end = -1;
        for (int i = 0; i < len; i++) {
            char c = path.charAt(i);
            if (begin == -1 && (c == '{' || c == '[' || c == '(')) {
                begin = i;
            } else if (c == '}' || c == ']' || c == ')') {
                end = i;
            }
        }
        return begin != -1 && end - begin > 1;
    }

    /**
     * 节点路径，记载了节点的路径、路径代码、相关联的值、子路径等信息。
     */
    private static class TreeNode<V> implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 节点的相对路径，可能重复不唯一（至根节点的绝对路径唯一） */
        private String path;

        /**
         * 根节点到此节点的路径代码（二进制）。根节点代码为1。
         * 0代表匹配，1代表确切字符串；多匹配路径取代码最大的为最匹配路径。
         */
        private int code = -1;

        /** 节点路径相关联的值 */
        private V value;

        /** 节点路径的匹配索引及键名数组，不含相关联值的节点为 null */
        private IndexKey[] indexKeys;

        /** 节点路径的子路径。叶子节点无子路径，且一定包含相关联的值 */
        private Map<String, TreeNode<V>> children;

        /**
         * 构造一个指定相对路径和相关联值的节点路径。
         *
         * @param path 相对与父节点的路径。
         * @param value 与节点相关联的值。
         */
        private TreeNode(String path, V value) {
            this.path = path;
            this.value = value;
        }

        /**
         * 获取子路径的节点。
         *
         * @param path 指定的子路径名称。
         *
         * @return 子路径的节点，如果没有则返回null。
         */
//        public TreeNode get(String path) {
//            return children == null ? null : children.get(path);
//        }

        /**
         * 在此节点上添加子路径。
         * 如果原有子路径节点已存在则返回原有的节点，否则添加并返回新增的子节点。
         *
         * @param paths 子节点全路径解析后的路径字符串数组。
         * @param child 指定的子路径名称。
         *
         * @return 新增子路径的节点；如果原子路径节点存在则返回原子路径节点。
         */
        private TreeNode<V> addBranch(final String[] paths, String child) {
            // 未创建子路径节点集合
            if (children == null) {
                children = new HashMap<>();
                TreeNode<V> newNode = new TreeNode<>(child, null);
                // set the new child node and put it in the children nodes
                setChildNode(newNode, paths);
                children.put(newNode.path, newNode);
                // 返回新增的节点
                return newNode;
            }

            // 查找子节点路径，如果路径包含键匹配，则查找路径为'*'
            TreeNode<V> old = children.get(isMatchKay(child) ? SINGLE_MATCH : child);
            // 子节点集合未包含此节点
            if (old == null) {
                TreeNode<V> newNode = new TreeNode<>(child, null);
                // set the new child node and put it in the children nodes
                setChildNode(newNode, paths);
                children.put(newNode.path, newNode);
                // 返回新增的节点
                return newNode;
            } else {
                // 返回已存在的节点
                return old;
            }
        }

        /**
         * 添加叶子节点的路径与其相关联的值。
         * 如果原叶子节点路径已存在值则返回其原有的值，否则返回 null。
         *
         * @param paths 子节点全路径解析后的路径字符串数组。
         * @param child 指定的子路径名称。
         * @param value 叶子节点路径相关联的值。
         *
         * @return 如果原叶子节点的值存在则返回原叶子节点的值，否则返回 null。
         */
        private V addLeaf(final String[] paths, String child, V value) {
            // 未创建子路径节点集合
            if (children == null) {
                children = new HashMap<>();
                TreeNode<V> newNode = new TreeNode<>(child, value);
                // set the new child node and put it in the children nodes
                setChildNode(newNode, paths);
                children.put(newNode.path, newNode);
                // 新增叶子节点返回 null
                return null;
            }

            // 查找子节点路径，如果路径包含键匹配，则查找路径为'*'
            TreeNode<V> old = children.get(isMatchKay(child) ? SINGLE_MATCH : child);
            // 子节点集合未包含此节点
            if (old == null) {
                TreeNode<V> newNode = new TreeNode<>(child, value);
                // set the new child node and put it in the children nodes
                setChildNode(newNode, paths);
                children.put(newNode.path, newNode);
                // 新增叶子节点返回 null
                return null;
            }
            /*
             * 覆盖原节点的值，并返回原有的值，如果原节点没有值返回 null
             */
            V oldValue = old.value;
            old.value = value;
            // 重新设置索引/索引/值数组
            setLeafIndexKeys(old, paths);
            return oldValue;
        }

        /**
         * 设置此节点下子节点路径的代码、索引/值类数组等信息。
         * 如果子节点路径包含键匹配，则将其路径名称设为'*'用于路径的查询。
         *
         * @param child 子节点。
         * @param paths 子节点全路径解析后的路径字符串数组。
         */
        private void setChildNode(TreeNode<V> child, String[] paths) {
            String childPath = child.path;
            // if child path is '*'
            child.code = (code << 1);
            if (child.code < 0) {
                throw new IllegalArgumentException("Depth of the tree is too large, no more than 32 layers.");
            }
            // not equals '*'
            if (!SINGLE_MATCH.equals(childPath)) {
                // 结果代表是否完全匹配的
                String key = getMatchKey(childPath);
                // 返回 null代表完全匹配的字符串
                if (key == null) {
                    // 为确切的字符串则此路径(二进制)末位为1
                    child.code += 1;
                } else {
                    // 如果路径包含键匹配，则设置路径为'*'，方便添加节点时遍历查询。
                    child.path = SINGLE_MATCH;
                }
            }

            // 如果为叶子节点，设置其索引/值数组
            setLeafIndexKeys(child, paths);
        }

        /**
         * 设置叶子节点的索引/值数组。
         * 并处理连续'*'路径（如果有）为*,*1,*2...
         *
         * @param leaf 叶子节点。
         * @param paths 节点全路径解析后的路径字符串数组。
         */
        private void setLeafIndexKeys(TreeNode<V> leaf, String[] paths) {
            // 仅叶子节点有相关联的值才添加索引/值数组
            if (leaf.value != null) {
                List<IndexKey> keys = new ArrayList<>(paths.length);
                byte matchIndex = 1;
                for (byte i = 0; i < paths.length; i++) {
                    if (SINGLE_MATCH.equals(paths[i])) {
                        // *,*1,*2...
                        keys.add(new IndexKey(i, matchIndex == 1 ? SINGLE_MATCH : SINGLE_MATCH + matchIndex));
                        matchIndex++;
                    } else {
                        // 路径的非完全匹配名称
                        String key = getMatchKey(paths[i]);
                        if (key != null) {
                            keys.add(new IndexKey(i, key));
                        }
                    }
                }
//                System.out.println("IndexKey : " + keys);
                if (!keys.isEmpty()) {
                    leaf.indexKeys = keys.toArray(new IndexKey[0]);
                }
//                System.out.println("setChildNode : " + child);
            }
        }

        @Override
        public String toString() {
            return "TreeNode{" + "path=" + path + ", code=" + Integer.toBinaryString(code) + ", value=" + value + ", children=" + children + '}';
        }
    }

    /**
     * 索引/值类。记录了节点匹配符在路径数组中的索引位置及匹配的键名。
     */
    private static class IndexKey implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * Path array index, no more than 32.
         */
        private byte index = -1;

        /**
         * 匹配的键名。
         */
        private String matchKey;

        /**
         * 构造一个指定索引号和键名的类。
         *
         * @param index 指定的索引号。
         * @param matchKey 指定的键名。
         */
        public IndexKey(byte index, String matchKey) {
            this.index = index;
            this.matchKey = matchKey;
        }

        @Override
        public String toString() {
            return "IndexKey{" + "index=" + index + ", matchKey=" + matchKey + '}';
        }
    }

    /**
     * 获取路径分隔符。
     *
     * @return 路径分隔符。
     */
    public char getPathSeparator() {
        return pathSeparator;
    }
}
