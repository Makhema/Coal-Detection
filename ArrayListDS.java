package coal.detection.datastructures;

// This class is an array list
public class ArrayListDS<T> {
    private Object[] elements;
    private int size;
    private int capacity;

    // Create an empty array list
    public ArrayListDS() {
        size = 0;
        capacity = 10;
        elements = new Object[capacity];
    }

    // Return the number of stored items
    public int size() {
        return size;
    }

    // Check whether the structure has no items
    public boolean isEmpty() {
        return size == 0;
    }

    @SuppressWarnings("unchecked")
    // Return the item at the given index
    public T get(int index) {
        if(!isValidIndex(index)) 
		{
            throw new RuntimeException("Index out of bounds");
        }
        return (T) elements[index];
    }

    @SuppressWarnings("unchecked")
    // Replace the item at the given index
    public T set(int index, T element) {
        if(!isValidIndex(index)) 
		{
            throw new RuntimeException("Index out of bounds");
        }
        T oldElement = (T) elements[index];
        elements[index] = element;
        return oldElement;
    }

    // Add an item to the list
    public void add(T element) {
        add(size, element);
    }

    // Add an item to the list
    public void add(int index, T element) {
        if(index < 0 || index > size) 
		{
            throw new RuntimeException("Index out of bounds");
        }
        ensureCapacity();
        for(int i = size - 1; i >= index; i--) 
		{
            elements[i + 1] = elements[i];
        }
        elements[index] = element;
        size++;
    }

    @SuppressWarnings("unchecked")
    // Remove and return an item
    public T remove(int index) {
        if(!isValidIndex(index)) {
            throw new RuntimeException("Index out of bounds");
        }
        T removed = (T) elements[index];
        for(int i = index; i < size - 1; i++) 
		{
            elements[i] = elements[i + 1];
        }
        elements[size - 1] = null;
        size--;
        return removed;
    }

    // Remove all items from the list
    public void clear() {
        for(int i = 0; i < size; i++) 
		{
            elements[i] = null;
        }
        size = 0;
    }

    // Make the array bigger when it becomes full
    private void ensureCapacity() {
        if(size < capacity) 
		{
            return;
        }
        capacity = capacity * 2;
        Object[] bigger = new Object[capacity];
        for(int i = 0; i < size; i++) 
		{
            bigger[i] = elements[i];
        }
        elements = bigger;
    }

    // Check if an index is inside the list
    private boolean isValidIndex(int index) {
        return index >= 0 && index < size;
    }
}
