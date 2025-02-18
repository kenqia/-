import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * key-value形式的红黑树的简单实现,该实现并没有借助NIL哨兵节点
 * 主要方法:
 * =====public=====
 * {@link RedBlackTree#RedBlackTree()}  构建红黑树对象，使用自然比较器
 * {@link RedBlackTree#RedBlackTree(Comparator)} 构建红黑树对象，使用自定义比较器
 * {@link RedBlackTree#put(Object, Object)}  存放k-v键值对，将按照key的大小排序
 * {@link RedBlackTree#remove(Object)}  根据key尝试查找并尝试删除对应的键值对
 * {@link RedBlackTree#toInorderTraversalString()} 中序遍历红黑树（顺序输出）
 * {@link RedBlackTree#minKey()} 获取最小的key
 * {@link RedBlackTree#maxKey()} 获取最大的key
 * {@link RedBlackTree#get(Object)} 根据key获取value
 * =====private=====
 * {@link RedBlackTree#binaryTreePut(RedBlackNode, RedBlackNode)}  使用二叉排序树的方式尝试插入节点
 * {@link RedBlackTree#rebalanceAfterPut(RedBlackNode)}  插入节点之后进行重平衡
 * {@link RedBlackTree#searchRemoveNode(Object, RedBlackNode)}  使用二叉排序树的方式尝试寻找真正需要被删除的节点
 * {@link RedBlackTree#removeNode(RedBlackNode)}  使用二叉排序的方式删除节点,并对部分简单情况进行重平衡
 * {@link RedBlackTree#rebalanceAfterRemove(RedBlackNode, RedBlackNode)}  对复杂情况进行重平衡
 * {@link RedBlackTree#rotateLeft(RedBlackNode)}  左旋
 * {@link RedBlackTree#rotateRight(RedBlackNode)}  右旋
 * {@link RedBlackTree#rotateLeftAndRight(RedBlackNode)}  左-右双旋
 * {@link RedBlackTree#rotateRightAndLeft(RedBlackNode)}  右-左双旋
 *
 * @author lx
 */
public class RedBlackTree<K, V> {


    /**
     * 自定义比较器
     */
    private Comparator<? super K> cmp;


    /**
     * 树节点的数量
     */
    private int size;


    /**
     * 红黑树的根节点，默认为null
     */
    private RedBlackNode<K, V> root;


    /**
     * 红黑树节点对象
     *
     * @param <K> key类型
     * @param <V> value类型
     */
    private static final class RedBlackNode<K, V> {
        /**
         * 节点颜色，true 红色 false 黑色
         */
        boolean red;
        /**
         * 关键字
         */
        K key;
        /**
         * 值
         */
        V value;
        /**
         * 左子节点
         */
        RedBlackNode<K, V> left;
        /**
         * 左子节点
         */
        RedBlackNode<K, V> right;
        /**
         * 父节点
         */
        RedBlackNode<K, V> parent;

        public RedBlackNode(boolean red, K key, V value, RedBlackNode<K, V> left, RedBlackNode<K, V> right) {
            this.red = red;
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;
        }

        public RedBlackNode(boolean red, K key, V value) {
            this.red = red;
            this.key = key;
            this.value = value;
        }

        public RedBlackNode(K key, V value) {
            this.key = key;
            this.value = value;
        }


        public RedBlackNode() {
        }

        public RedBlackNode(RedBlackNode<K, V> parent) {
            this.parent = parent;
        }

        @Override
        public String toString() {
            return "{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }
    }


    /**
     * 空的构造器,使用自然顺序比较
     */
    public RedBlackTree() {
    }


    /**
     * 指定比较器
     *
     * @param cmp 指定比较器
     */
    public RedBlackTree(Comparator<? super K> cmp) {
        this.cmp = cmp;
    }


    /**
     * 插入,开放给外部使用的api
     * 插入步骤大概分为两步：
     * 1)	使用二叉查找树的插入方法，将红色节点插入；
     * 2)	如果此时树结构不符合红黑树的要求，那么通过旋转和重新着色等一系列操作使之重新成为一颗红黑树。
     *
     * @param key   插入的key
     * @param value 插入的value
     */
    public void put(K key, V value) {
        /*0检查key*/
        checkKey(key);
        /*1二叉排序树的插入，需要增加的步骤是记录父节点*/
        //创建被插入的节点,默认红色
        RedBlackNode<K, V> kvRedBlackNode = new RedBlackNode<>(true, key, value, null, null);
        //返回root,但此时新的节点可能已经被插入进去了
        int oldSize = size;
        root = binaryTreePut(kvRedBlackNode, root);
        /*2如果确实插入了节点元素,那么调整平衡*/
        if (size > oldSize) {
            rebalanceAfterPut(kvRedBlackNode);
        }
    }

    /**
     * 根据key删除键值对，大概分为两步：
     * 1)	通过二叉排序树的删除方法，将该节点从红黑树中删除；
     * 2)	如果此时树结构不符合红黑树的要求，那么通过旋转和重新着色等一系列手段使之重新成为一棵红黑树。
     * <p>
     * 该实现实际上分为三步:
     * searchRemoveNode: 使用二叉排序的删除方法,尝试寻找需要真正被删除的Node节点，没有进行删除
     * removeNode: 用二叉排序的方法删除节点,并进行部分调整,对于复杂的情况,返回需要进行重平衡的c节点
     * rebalanceAfterRemove: 如果c不等于null,说明被删除节点N是黑色节点且需要进行进一步的重平衡
     *
     * @param key 需要删除的key
     * @return 被删除的key对应的value
     */
    public V remove(K key) {
        checkKey(key);
        /*1使用二叉排序的删除方法,尝试寻找需要真正被删除的Node节点，没有进行删除*/
        RedBlackNode<K, V> removeNode = searchRemoveNode(key, root);
        if (removeNode == null) {
            return null;
        }
        V value = removeNode.value;
        /*2、用二叉排序的方法删除节点,并进行部分调整,对于复杂的情况,返回需要进行重平衡的c节点*/
        RedBlackNode<K, V> n = removeNode(removeNode);
        /*3、如果c不等于null,说明被删除节点N是黑色节点且需要进行进一步的重平衡*/
        if (n != null) {
            rebalanceAfterRemove(n.right, n.parent);
        }
        return value;
    }


    /**
     * 用于删除节点并且进行部分重平衡：对于“删红”，“删黑子红”，“删黑子黑-删根”这三种简单的情况进行了判断和重平衡
     *
     * @param n 真正需要被删除的节点,该节点要么没有子节点,要么只有一个左或者右子节点
     * @return 还需要进一步被重平衡操作的节点，或者null-表示不需要进一步重平衡操作
     */
    private RedBlackNode<K, V> removeNode(RedBlackNode<K, V> n) {
        /*首先删除子节点*/
        RedBlackNode<K, V> left = n.left;
        RedBlackNode<K, V> right = n.right;
        RedBlackNode<K, V> parent = n.parent;
        //n的子节点
        RedBlackNode<K, V> child = null;
        /*如果父节点不为null*/
        if (parent != null) {
            //如果n是父节点的左子节点
            if (parent.left == n) {
                //如果n的左右子节点都为null
                if (left == null && right == null) {
                    parent.left = null;
                    child = null;
                }
                //如果n的左子节点不为null
                else if (left != null) {
                    parent.left = left;
                    left.parent = parent;
                    child = left;
                }
                //如果n的右子节点不为null
                else {
                    parent.left = right;
                    right.parent = parent;
                    child = right;
                }
            }
            //如果n是父节点的右子节点
            if (parent.right == n) {
                //如果n的左右子节点都为null
                if (left == null && right == null) {
                    parent.right = null;
                    child = null;
                }
                //如果n的左子节点不为null
                else if (left != null) {
                    parent.right = left;
                    left.parent = parent;
                    child = left;
                }
                //如果n的右子节点不为null
                else {
                    parent.right = right;
                    right.parent = parent;
                    child = right;
                }
            }
        }
        /*如果父节点为null,则说明要删除的节点是根节点*/
        else {
            //如果n的左右子节点都为null
            if (left == null && right == null) {
                root = null;
                child = null;
            }
            //如果n的左子节点不为null
            else if (left != null) {
                left.parent = null;
                root = left;
                child = left;
            }
            //如果n的右子节点不为null
            else {
                right.parent = null;
                root = right;
                child = right;
            }
        }
        /*1 被删除的节点为红色，这里处理了；*/
        if (n.red) {
            /*1.2由于是N红色节点，删除N后不会影响红黑树的平衡性，与其他节点的颜色无关，因此不需要做任何调整。*/
            return null;
        }
        /*2 被删除的节点为黑色，子节点为红色，这里处理了；*/
        else if (child != null && child.red) {
            /*2.1首先删除节点*/
            /*2.2将后继C涂黑，平衡调整完成。*/
            child.red = false;
            return null;
        }
        /*3 被删除的节点为黑色，子节点为黑色；
         * 这里又要分为多种情况：
         * 1)	被删除节点N是根节点，简称“删根”，这里处理了；
         * 2)	被删除节点N的兄弟节点B为红色，简称“删兄红”，这里没有处理，在rebalanceAfterRemove方法中处理；
         * 3)	被删除节点N的兄弟节点B为黑色(null也看成黑色)，简称“删兄黑”，这里没有处理，在rebalanceAfterRemove方法中处理；
         * */
        else {
            /*3.1 被删除节点N是根节点，简称“删根”*/
            /*那好办，任何无需平衡操作*/
            if (n.parent == null) {
                return null;
            }
            /*剩下两种复杂情况,此时需要进一步进行复杂的平衡操作*/
            //将n的子节点(null或左子节点或右子节点)设置为右子节点,方便后面调整的时候取出来
            n.right = child;
            return n;
        }
    }


    /**
     * 删除节点之后进行重平衡,用于处理下面两种复杂的情况
     * 2)	被删除节点N的兄弟节点B为红色，简称“删兄红”；
     * 3)	被删除节点N的兄弟节点B为黑色(null也看成黑色)，简称“删兄黑”；
     *
     * @param c 需要进行平衡的节点
     * @param p 需要进行平衡的节点的父节点
     */
    private void rebalanceAfterRemove(RedBlackNode<K, V> c, RedBlackNode<K, V> p) {
        //获取兄弟节点
        RedBlackNode<K, V> brother;
        /*如果c是左子节点,那么brother就是右兄弟*/
        if (c == p.left) {
            brother = p.right;
            /*3.2 被删除节点N的兄弟节点为红色，简称删兄红； 且是右兄弟*/
            if (brother != null && brother.red) {
                /*以父节点P为基点左旋*/
                rotateLeft(p);
                /*然后B涂黑P涂红（交换颜色）*/
                brother.red = false;
                p.red = true;
                /*最后将BL看成新的兄弟节点newB；将情况二转换为情况三*/
                brother = p.right;
            }
            /*3.3 如果被删除节点N的兄弟节点为黑色，简称删兄黑；且是右兄弟*/
            /*3.3.1	兄节点的子节点不全是黑色节点，简称兄子非全黑；下面针对右兄弟分为两种情况*/
            if (brother != null) {
                //排除都为null
                if (!(brother.right == null && brother.left == null)) {
                    //排除都为黑色
                    if (!(brother.right != null && !brother.right.red && brother.left != null && !brother.left.red)) {
                        /*3.3.1.1	兄弟节点在右边，且兄右子节点为黑色；*/
                        if (brother.right == null || !brother.right.red) {
                            if (brother.left != null) {
                                brother.left.red = false;
                            }
                            brother.red = true;
                            /*以兄弟节点B为基点右旋*/
                            rotateRight(brother);
                            /*将BL当成新B，B当成新BR，这样就转换成了情况2。*/
                            brother = p.right;
                        }
                        /*3.3.1.2	兄弟节点在右边，且兄右子节点为红色；*/
                        if (brother.right != null && brother.right.red) {
                            /*交换P和B的颜色*/
                            boolean color = p.red;
                            p.red = brother.red;
                            brother.red = color;
                            /*BR涂黑*/
                            brother.right.red = false;
                            /*以父节点P为基点左旋*/
                            rotateLeft(p);
                            return;
                        }
                    }
                }
            }
        }
        /*如果c是右子节点,那么brother就是左兄弟*/
        else {
            brother = p.left;
            /*3.2 被删除节点N的兄弟节点为红色，简称删兄红；且是左兄弟*/
            if (brother != null && brother.red) {
                /*以父节点P为基点右旋，然后B涂黑P涂红（交换颜色），最后将BR看成新的兄弟节点newB；
                 * 将情况二转换为情况三*/
                /*以父节点P为基点右旋*/
                rotateRight(p);
                /*然后B涂黑P涂红（交换颜色）*/
                brother.red = false;
                p.red = true;
                /*最后将BR看成新的兄弟节点newB；将情况二转换为情况三*/
                brother = p.left;
            }
            /*3.3 如果被删除节点N的兄弟节点为黑色，简称删兄黑；且是左兄弟*/
            /*3.3.1	兄节点的子节点不全是黑色节点，简称兄子非全黑；下面针对左兄弟分为两种情况*/
            if (brother != null) {
                //排除都为null
                if (!(brother.right == null && brother.left == null)) {
                    //排除都为黑色
                    if (!(brother.right != null && !brother.right.red && brother.left != null && !brother.left.red)) {
                        /*3.3.1.3	兄弟节点在左边，且兄左子节点为黑色；*/
                        if (brother.left == null || !brother.left.red) {
                            if (brother.right != null) {
                                brother.right.red = false;
                            }
                            brother.red = true;
                            /*以兄弟节点B为基点左旋*/
                            rotateLeft(brother);
                            /*将BR当成新B，B当成新BL，这样就转换成了情况4。*/
                            brother = p.left;
                        }
                        /*3.3.1.4	兄弟节点在左边，且兄左子节点为红色；*/
                        if (brother.left != null && brother.left.red) {
                            /*交换P和B的颜色*/
                            boolean color = p.red;
                            p.red = brother.red;
                            brother.red = color;
                            /*BL涂黑*/
                            brother.left.red = false;
                            /*以父节点P为基点右旋*/
                            rotateRight(p);
                            return;
                        }
                    }
                }
            }
        }
        /*3.3.2	兄节点的子节点全是黑色节点，简称兄子全黑；*/
        /*如果兄弟节点为null，也算全黑*/
        if (brother == null) {
            /*3.3.2.1	父节点P是黑色；*/
            /*如果父节点还有父节点，那么进行递归，否则没有意义*/
            if (p.parent != null && !p.red) {
                rebalanceAfterRemove(p, p.parent);
            }
            /*3.3.2.2	父节点P是红色；*/
            else {
                p.red = false;
            }
        } else {
            //都为null
            if (brother.left == null && brother.right == null) {
                /*3.3.2.1	父节点P是黑色；*/
                /*如果父节点还有父节点，那么进行递归，否则没有意义*/
                if (p.parent != null && !p.red) {
                /*将兄弟节点B涂红，将父节点P设为新的C节点，将U设为新B节点，将G设为新P节点，
                回到删黑子黑的情况，即向上递归进行处理，直到C成为根节点或者达到平衡。*/
                    brother.red = true;
                    rebalanceAfterRemove(p, p.parent);
                }
                /*3.3.2.2	父节点P是红色；*/
                else {
                    brother.red = true;
                    p.red = false;
                }
            }
            //都为黑色
            else if (brother.left != null && brother.right != null && !brother.left.red && !brother.right.red) {
                /*3.3.2.1	父节点P是黑色；*/
                /*如果父节点还有父节点，那么进行递归，否则没有意义*/
                if (p.parent != null && !p.red) {
                /*将兄弟节点B涂红，将父节点P设为新的C节点，将U设为新B节点，将G设为新P节点，
                回到删黑子黑的情况，即向上递归进行处理，直到C成为根节点或者达到平衡。*/
                    brother.red = true;
                    rebalanceAfterRemove(p, p.parent);
                }
                /*3.3.2.2	父节点P是红色；*/
                else {
                    brother.red = true;
                    p.red = false;
                }
            }
        }

    }

    /**
     * 尝试寻找需要真正被删除的Node节点
     *
     * @param key  匹配的key
     * @param root 从根节点开始递归查找
     * @return 找到的节点, 或者为null, 表示没找到
     */
    private RedBlackNode<K, V> searchRemoveNode(K key, RedBlackNode<K, V> root) {
        if (root == null) {
            return null;
        }
        /*调用比较的方法*/
        int i = compare(key, root.key);
        /*如果大于0，则说明e>root.data 继续查询右子树*/
        if (i > 0) {
            return searchRemoveNode(key, root.right);
        }
        /*如果小于0，则说明e<root.data 继续查询左子树*/
        else if (i < 0) {
            return searchRemoveNode(key, root.left);
        } else {
            /*如果等于0，则说明key=root 即查询成功 开始做好删除的准备,返回真正需要被删除的节点*/
            size--;
            /*如果两个子节点都不为null*/
            if (root.left != null && root.right != null) {
                /*递归查找最小的节点*/
                RedBlackNode<K, V> min = findMin(root.right);
                /*然后交换元素值*/
                K tempKey = min.key;
                min.key = root.key;
                root.key = tempKey;
                V tempValue = min.value;
                min.value = root.value;
                root.value = tempValue;
                /*返回真正需要被删除的节点,即在右子树中找到的大于root的最小节点*/
                return min;
            } else {
                /*如果一个子节点不为null，则返回该子节点；或者两个子节点都为null，则返回该节点*/
                return root;
            }
        }
    }

    /**
     * 查找真正被删除的最小的节点
     *
     * @param root 根节点
     * @return 最小的节点
     */
    private RedBlackNode<K, V> findMin(RedBlackNode<K, V> root) {
        /*如果该节点没有左子节点，那么该节点就是最小的节点，返回*/
        if (root.left == null) {
            return root;
        }
        /*如果该节点存在左子节点，那么继续向左递归查找*/
        return findMin(root.left);
    }


    /**
     * 查找最小的key
     *
     * @return 最小的节点的key
     */
    public K minKey() {
        if (root == null) {
            return null;
        }
        /*如果该节点存在左子节点，那么继续向左递归查找*/
        return findMin(root).key;
    }

    private RedBlackNode<K, V> findMax(RedBlackNode<K, V> root) {
        /*如果该节点没有右子节点，那么该节点就是最小的节点，返回*/
        if (root.right == null) {
            return root;
        }
        /*如果该节点存在左子节点，那么继续向左递归查找*/
        return findMax(root.right);
    }

    /**
     * 查找最大的key
     *
     * @return 最大的节点的key
     */
    public K maxKey() {
        if (root == null) {
            return null;
        }
        /*如果该节点存在左子节点，那么继续向左递归查找*/
        return findMax(root).key;
    }


    /**
     * 根据key,查找value
     *
     * @return 最大的节点的key
     */
    public V get(K Key) {
        /*如果该节点存在左子节点，那么继续向左递归查找*/
        RedBlackNode<K, V> kvRedBlackNode = searchRemoveNode(Key, root);
        if (kvRedBlackNode != null) {
            return kvRedBlackNode.value;
        }
        return null;
    }


    /**
     * 对元素进行比较大小的方法,如果传递了自定义比较器,则使用自定义比较器,否则则需要数据类型实现Comparable接口
     *
     * @param k1 被比较的第一个对象
     * @param k2 被比较的第二个对象
     * @return 0 相等 ;小于0 k1 < k2 ;大于0 k1 > k2
     */
    private int compare(K k1, K k2) {
        if (cmp != null) {
            return cmp.compare(k1, k2);
        } else {
            return ((Comparable<K>) k1).compareTo(k2);
        }
    }

    /**
     * 添加节点之后再平衡，需要分多种情况讨论:
     * 1)	新插入节点N作为根基点，简称“新根”；
     * 2)	新插入节点N的父节点为黑色，简称“父黑”；
     * 3)	新插入节点N的父节点为红色，叔节点为黑色，简称“父红叔黑”；
     * 4)	新插入节点N的父节点为红色，叔节点为红色，简称“父红叔红”；
     *
     * @param newNode 新增加的节点
     */
    private void rebalanceAfterPut(RedBlackNode<K, V> newNode) {
        //获取父节点
        RedBlackNode<K, V> parent = newNode.parent;
        /*1 新插入节点N作为根基点，简称“新根”*/
        if (parent == null) {
            //直接涂黑即可
            newNode.red = false;
            return;
        }
        /*2 新插入节点N的父节点为黑色，简称“父黑”*/
        else if (!parent.red) {
            //无需调整
            return;
        }
        /*3 新插入节点N的父节点为红色 下面需要分情况讨论*/
        /*3.1首先获取一系列与新节点相关的的节点*/
        //获取祖父节点
        RedBlackNode<K, V> grandParent = parent.parent;
        //获取叔节点
        RedBlackNode<K, V> uncle = parent == grandParent.left ? grandParent.right : grandParent.left;
        /*3.2 叔节点为红色，简称“父红叔红”*/

        if (uncle != null && uncle.red) {
            /*将newNode节点的父节点叔节点颜色染成黑色，祖父节点染成红色。*/
            parent.red = false;
            uncle.red = false;
            grandParent.red = true;
            /*此时祖父节点可能与其父节点颜色冲突，因此需要递归的解决*/
            rebalanceAfterPut(grandParent);
        }
        /*3.3 如果叔节点为黑色,，简称“父红叔黑” 需要分四种情况讨论
         * 1)	在祖父节点G的左孩子节点P的左子树中插入节点N，简称“LL”； 右旋,换色
         * 2)	在祖父节点G的左孩子节点P的右子树中插入节点N，简称“LR”； 左旋+右旋,换色
         * 3)	在祖父节点G的右孩子节点P的左子树中插入节点N，简称“RL”； 右旋+左旋,换色
         * 4)	在祖父节点G的右孩子节点P的右子树中插入节点N，简称“RR”。 左旋,换色
         * */
        else {
            /*3.3.1 在祖父节点G的左孩子节点P的左子树中插入节点N，简称“LL”； 右旋,换色*/
            if (grandParent.left == parent && parent.left == newNode) {
                /*P涂黑，G涂红*/
                parent.red = false;
                grandParent.red = true;
                /*以G为基点右旋*/
                rotateRight(grandParent);
            }
            /*3.3.2 在祖父节点G的左孩子节点P的右子树中插入节点N，简称“LR”； 左旋+右旋,换色*/
            else if (grandParent.left == parent && parent.right == newNode) {
                /*N涂黑，G涂红*/
                newNode.red = false;
                grandParent.red = true;
                /*左-右双旋*/
                rotateLeftAndRight(grandParent);
            }
            /*3.3.3 在祖父节点G的右孩子节点P的左子树中插入节点N，简称“RL”； 右旋+左旋,换色*/
            else if (grandParent.right == parent && parent.left == newNode) {
                /*N涂黑，G涂红*/
                newNode.red = false;
                grandParent.red = true;
                /*右-左双旋*/
                rotateRightAndLeft(grandParent);

            }
            /*3.3.4 在祖父节点G的右孩子节点P的右子树中插入节点N，简称“RR”。 左旋,换色*/
            else {
                /*P涂黑，G涂红*/
                parent.red = false;
                grandParent.red = true;
                /*以G为基点左旋*/
                rotateLeft(grandParent);
            }
        }
    }

    /**
     * LL,右旋,类似于AVL树的右旋操作,但是需要更新父节点,并且不需要返回值,因为这并不是递归的再平衡
     * 这里的k1k2对应着右旋模型中的点位
     * 通解:右旋之后，k2成为根节点，k1成为k2的右子节点，k2的右子树2成为k1的左子树
     *
     * @param k1 右旋的基点
     */
    private void rotateRight(RedBlackNode<K, V> k1) {
        //获取基点的父节点
        RedBlackNode<K, V> parent = k1.parent;
        //获取k2,k2是k1的左子节点
        RedBlackNode<K, V> k2 = k1.left;
        //k2的右子树成为k1的左子树
        k1.left = k2.right;
        //如果k2的右子树不是null树，则更新右子树的父节点的引用
        if (k2.right != null) {
            k2.right.parent = k1;
        }
        //k1成为k2的右子节点
        k2.right = k1;
        //更新k1的父节点的引用
        k1.parent = k2;
        //如果k1之前是根节点,则k2成为根节点,更新根节点的引用
        if (parent == null) {
            this.root = k2;
            //如果k1是父节点的左子节点,那么更新父节点的左子节点的引用
        } else if (k1 == parent.left) {
            parent.left = k2;
        } else {
            //如果k1是父节点的右子节点,那么更新父节点的右子节点的引用
            parent.right = k2;
        }
        //最后更新k2的父节点的引用
        k2.parent = parent;
    }


    /**
     * RR,左旋,类似于AVL树的左旋操作,属于右旋的镜像，但是需要更新父节点,并且不需要返回值,因为这并不是递归的再平衡
     * 这里的k1k2对应着左旋模型中的点位
     * 通解:左旋之后，k2成为根节点，k1成为k2的左子节点，k2的左子树2成为k1的右子树
     *
     * @param k1 左旋的基点
     */
    private void rotateLeft(RedBlackNode<K, V> k1) {
        //获取基点的父节点
        RedBlackNode<K, V> parent = k1.parent;
        //获取k2,k2是k1的右子节点
        RedBlackNode<K, V> k2 = k1.right;
        //k2的左子树成为k1的右子树
        k1.right = k2.left;
        //如果k2的左子树不是null树，则更新左子树的父节点的引用
        if (k2.left != null) {
            k2.left.parent = k1;
        }
        //k1成为k2的左子节点
        k2.left = k1;
        //更新k1的父节点的引用
        k1.parent = k2;
        //如果k1之前是根节点,则k2成为根节点,更新根节点的引用
        if (parent == null) {
            this.root = k2;
            //如果k1是父节点的左子节点,那么更新父节点的左子节点的引用
        } else if (k1 == parent.left) {
            parent.left = k2;
        } else {
            //如果k1是父节点的右子节点,那么更新父节点的右子节点的引用
            parent.right = k2;
        }
        //最后更新k2的父节点的引用
        k2.parent = parent;
    }

    /**
     * RL,右-左双旋，类似于AVL树的右-左双旋
     * 通解：将k3当作新的根节点，并且先使得k2右旋成为k3的右子树，然后k1左旋成为k3的左子树，并且左子树2成为k1的右子树，右子树2成为k2的左子树
     *
     * @param k1 需要旋转的最小不平衡树根节点
     */
    private void rotateRightAndLeft(RedBlackNode<K, V> k1) {
        /*1先对k1的右子节点k2进行右旋,然后使得成为k3成为的k1的左子树*/
        rotateRight(k1.right);
        /*2然后对k1进行左旋,成为k3的左子树,返回的根节点就是k3,即返回旋转之后的根节点*/
        rotateLeft(k1);
    }


    /**
     * LR，左-右双旋，类似于AVL树的左-右双旋，很简单,实际上就是右-左双旋的镜像
     * 通解: 将k3当作新的根节点，并且先使得k2左旋成为k3的左子树，然后k1右旋成为k3的右子树，并且左子树2成为k2的右子树，右子树2成为k1的左子树
     *
     * @param k1 需要旋转的最小不平衡树根节点
     */
    private void rotateLeftAndRight(RedBlackNode<K, V> k1) {
        /*1先对k1的左子节点k2进行左旋,使得k3成为的k1的左子树*/
        rotateLeft(k1.left);
        /*2然后对k1进行右旋,成为k3的右子树,此时根节点就变成了k3*/
        rotateRight(k1);
    }

    /**
     * 检查key
     *
     * @param key key
     */
    private void checkKey(K key) {
        if (key == null) {
            throw new NullPointerException("key为null");
        }
    }


    /**
     * 二叉排序树的方式插入节点，同时记录父节点
     *
     * @param kvRedBlackNode 需要被插入的节点
     * @param root           根节点
     */
    private RedBlackNode<K, V> binaryTreePut(RedBlackNode<K, V> kvRedBlackNode, RedBlackNode<K, V> root) {
        /*没有查找到,那么直接构建新的节点返回,将会在上一层方法中被赋值给其父节点的某个引用,这个插入的位置肯定是该遍历路径上的最后一点
         * 即插入的元素节点肯定是属于叶子节点*/
        if (root == null) {
            size++;
            return kvRedBlackNode;
        }
        /*调用比较的方法*/
        int i = compare(kvRedBlackNode.key, root.key);
        /*如果大于0，则说明kvRedBlackNode>root 继续查询右子树*/
        if (i > 0) {
            //增加更新父节点的操作
            kvRedBlackNode.parent = root;
            root.right = binaryTreePut(kvRedBlackNode, root.right);
            /*如果小于0，则说明kvRedBlackNode<root 继续查询左子树*/
        } else if (i < 0) {
            //增加更新父节点的操作
            kvRedBlackNode.parent = root;
            root.left = binaryTreePut(kvRedBlackNode, root.left);
        } else {
            /*如果等于0，则说明kvRedBlackNode=root 即已经存在节点 替换value*/
            root.value = kvRedBlackNode.value;
        }
        //没查询到最底层,则返回该节点
        return root;
    }


    /**
     * 保存遍历出来的节点数据
     */
    List<RedBlackNode<K, V>> str = new ArrayList<>();

    /**
     * 中序遍历,提供给外部使用的api
     *
     * @return 遍历的数据
     */
    public String toInorderTraversalString() {
        //如果是空树,直接返回空
        if (root == null) {
            return null;
        }
        //从根节点开始递归
        inorderTraversal(root);
        //获取遍历结果
        String s = str.toString();
        str.clear();
        return s;
    }

    /**
     * 中序遍历 内部使用的递归遍历方法,借用了栈的结构
     *
     * @param root 节点,从根节点开始
     */
    private void inorderTraversal(RedBlackNode<K, V> root) {

        RedBlackNode<K, V> left = getLeft(root);
        if (left != null) {
            //如果左子节点不为null,则继续递归遍历该左子节点
            inorderTraversal(left);
        }
        //添加数据节点
        str.add(root);
        //获取节点的右子节点
        RedBlackNode<K, V> right = getRight(root);
        if (right != null) {
            //如果右子节点不为null,则继续递归遍历该右子节点
            inorderTraversal(right);
        }
    }


    /**
     * 获取左子节点
     *
     * @param parent 父节点引用
     * @return 左子节点或者null--表示没有左子节点
     */
    private RedBlackNode<K, V> getLeft(RedBlackNode<K, V> parent) {
        return parent == null ? null : parent.left;
    }

    /**
     * 获取右子节点
     *
     * @param parent 父节点引用
     * @return 右子节点或者null--表示没有右子节点
     */
    private RedBlackNode<K, V> getRight(RedBlackNode<K, V> parent) {
        return parent == null ? null : parent.right;
    }
}
