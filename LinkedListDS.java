package coal.detection.datastructures;


/**
 *@author - Motsoetsoana Makhema, Pedume Madisa, Mayibongwe Mathonsi, Samuel Chichongue.
 *
 */
// This class is a linked list used by the queue
public class LinkedListDS<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    // Create an empty linked list
    public LinkedListDS() {
        head = new Node<T>(null, null);
        tail = new Node<T>(null, null);
        head.setNext(tail);
        size = 0;
    }

    // Check whether the structure has no items
    public boolean isEmpty() {
        return size == 0;
    }

    // Return the number of stored items
    public int size() {
        return size;
    }

    // Add a new item at the end of the linked list
    public Node<T> addLast(T item) {
        Node<T> current = head;
        while (current.getNext() != tail)
        {
            current = current.getNext();
        }
        Node<T> newNode = new Node<T>(tail, item);
        current.setNext(newNode);
        size++;
        return newNode;
    }

    // Return the first node in the linked list
    public Node<T> first() {
        if(isEmpty())
        {
            throw new RuntimeException("The linked list is empty");
        }
        return head.getNext();
    }

    // Remove and return an item
    public T remove(Node<T> node) {
        if(isEmpty())
        {
            throw new RuntimeException("The linked list is empty");
        }
        if(node == null || node == head || node == tail) 
		{
            throw new RuntimeException("Invalid node");
        }
        Node<T> previous = head;
        Node<T> current = head.getNext();
        while(current != tail)
        {
            if (current == node)
            {
                previous.setNext(current.getNext());
                size--;
                return current.element();
            }
            previous = current;
            current = current.getNext();
        }
        throw new RuntimeException("Node not found");
    }
}
