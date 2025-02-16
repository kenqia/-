/**
 * 优先队列
 * 利用最大堆实现
 *
 * @author ronglexie
 * @version 2018/8/25
 */
public class PriorityQueue<E extends Comparable<E>> {

	private MaxHeap<E> maxHeap;

	public PriorityQueue() {
		maxHeap = new MaxHeap<>();
	}

	public int getSize() {
		return maxHeap.size();
	}

	public boolean isEmpty() {
		return maxHeap.isEmpty();
	}

	public void enqueue(E e) {
		maxHeap.add(e);
	}

	public E dequeue() {
		return maxHeap.extractMax();
	}

	public E getFront() {
		return maxHeap.findMax();
	}
}
