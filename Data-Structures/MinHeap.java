/**1
 * 最小堆
 * 完全二叉树实现、树中的根结点都表示树中的最小元素结点
 * 数组实现
 * @author kenqia
 * @version 2025/2/14
 */
public class MinHeap<E extends Comparable<E>> {

	private Array<E> data;

	public MinHeap(int capacity){
		data = new Array<>(capacity);
	}

	public MinHeap() {
		data = new Array<>();
	}

	public int size(){
		return data.getSize();
	}

	public boolean isEmpty(){
		return data.isEmpty();
	}

	/**
	 * 查找用数组实现的完全二叉树中该索引下节点的父亲节点的索引
	 *
	 *
	 * @param index
	 * @return int
	 * @author kenqia
	 * @version 2025/2/19
	 *
	 * 0 5 1 8 8 6 7 9 10 10
	 * 0 1 2 3 4 5 6 7 8 9
	 * 按照自上而下排序树节点
	 */
	public int parent(int index){
		if(index == 0){
			throw new IllegalArgumentException("root doesn't have parent.");
		}
		return (index - 1) / 2;
	}

	/**
	 * 查找用数组实现的完全二叉树中该索引下节点的左孩子节点的索引
	 * 可能出界
	 * @param index
	 * @return int
	 * @author kenqia
	 * @version 2025/2/14
	 */
	public int leftChild(int index){
		return (index * 2) + 1;
	}

	/**
	 * 查找用数组实现的完全二叉树中该索引下节点的右孩子节点的索引
	 * 可能出界
	 * @param index
	 * @return int
	 * @author kenqia
	 * @version 2025/2/14
	 */
	public int rightChild(int index){
		return (index * 2) + 2;
	}

	/**
	 * 向最小堆中添加元素
	 *
	 * @param e
	 * @return void
	 * @author kenqia
	 * @version 2025/2/14
	 */

	public void add(E e){
		data.addLast(e); //这边加到最后面 都是排序好的节点
		shifUp(data.getSize() - 1);
	}

	/**
	 * 上浮节点
	 *
	 * @param k
	 * @return void
	 * @author kenqia
	 * @version 2025/2/14
	 */
	private void shifUp(int k) {
		while (k > 0 && data.get(parent(k)).compareTo(data.get(k)) > 0){
			data.swap(k,parent(k));
			k = parent(k);
		}
	}

	/**
	 * 查找堆中最小值
	 *
	 * @param
	 * @return E
	 * @author kenqia
	 * @version 2025/2/14
	 */
	public E findMin(){
		if(data .getSize() == 0){
			throw new IllegalArgumentException("FindMax failed. heap is empty.");
		}
		return data.get(0);
	}

	/**
	 * 取出最小值
	 *
	 * @param
	 * @return E
	 * @author kenqia
	 * @version 2025/2/14
	 */
	public E extractMin(){
		E result = findMin();

		data.swap(0,data.getSize() - 1);
		data.removeLast();
		siftDown(0);

		return result;
	}

	/**
	 * 下沉节点
	 *
	 * @param k
	 * @return void
	 * @author kenqia
	 * @version 2025/2/14
	 */
	private void siftDown(int k) {
		while (k >= 0 && leftChild(k) < data.getSize()){
			int j = leftChild(k);
			//找到k节点的左右子节点的最大值j
			if (j + 1 < data.getSize() && data.get(j + 1).compareTo(data.get(j)) < 0) {
				j = rightChild(k);
			}
			//比较大小判断是否还需要下沉操作
			if(data.get(k).compareTo(data.get(j)) < 0){
				break;
			}
			data.swap(k,j);
			k = j;
		}
	}

}
