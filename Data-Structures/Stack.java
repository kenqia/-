/**1
 * 栈
 * 
 * @author kenqia
 * @version 2025/2/14
 */
public interface Stack<E> {

	/**
	 * 获取栈的大小
	 * 
	 * @param  
	 * @return int
	 * @author kenqia
	 * @version 2025/2/14
	 */
	int getSize();
	
	/**
	 * 判断栈是否为空
	 *
	 * @param
	 * @return boolean
	 * @author kenqia
	 * @version 2025/2/14
	 */
	boolean isEmpty();

	/**
	 * 向栈中插入一个元素
	 *
	 * @param e
	 * @return void
	 * @author kenqia
	 * @version 2025/2/14
	 */
	void push(E e);

	/**
	 * 向栈中移除一个元素
	 *
	 * @param
	 * @return E
	 * @author kenqia
	 * @version 2025/2/14
	 */
	E pop();

	/**
	 * 查看栈顶元素
	 *
	 * @param
	 * @return E
	 * @author kenqia
	 * @version 2025/2/14
	 */
	E peek();
}
