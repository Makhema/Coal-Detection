package acsse.csc3a.datastructures;

// This classs is a single node used in the linked list
public class Node<T> {
    private T element;
    private Node<T> next;

    // Create a node with a value and next link
    public Node(Node<T> next, T element) {
        this.next = next;
        this.element = element;
    }

    // Return the value stored in this node
    public T element() {
        return element;
    }

    // Return the next node
    public Node<T> getNext() {
        return next;
    }

    // Change the value stored in this node
    public void setElement(T element) {
        this.element = element;
    }

    // Change the next node link
    public void setNext(Node<T> next) {
        this.next = next;
    }
}
