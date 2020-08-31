package com.example.commons;
import java.io.Serializable;

public class List<T> implements Serializable{

    private Node<T> first;
    // Optimization for inserting and getting lasts
    private Node<T> last;
    // Optimization for length
    private int length;

    public Node<T> getFirst() {
        return first;
    }

    public Node<T> getLast() {
        return last;
    }

    // Constructor
    // Initializes a empty list
    public List(){
    }

    // return the length of the list
    public int length(){
        return this.length;
    }

    // Append a object to the end of the list
    public void addLast(T obj){

        if(this.length > 0){
            // there are other elements in the list, so append it to final
            Node<T> newNode = new Node<T>(obj);
            Node<T> oldNode = this.last;
            oldNode.next = newNode;
            this.last = newNode;
        }else{
            // the element is the unique element of the list
            Node<T> newNode = new Node<T>(obj);
            this.first = newNode;
            this.last = newNode;
        }

        this.length++;
    }

    // Insert a object the beginning of the list
    public void addFirst(T obj){
        if(this.length > 0){
            // there are other elements in the list, so append it to beginnig
            Node<T> newNode = new Node<T>(obj);
            newNode.next = this.first;
            this.first = newNode;
        }else{
            // the element is the unique element of the list
            Node<T> newNode = new Node<T>(obj);
            this.first = newNode;
            this.last = newNode;
        }

        this.length++;
    }

    // Insert a object to an specified index of the list
    public void add(int index, T obj) throws IndexOutOfBoundsException{
        if(index < 0 || index > this.length){
            // the index is invalid, throw an Exception
            throw new IndexOutOfBoundsException("Index: "+ index);
        }else if(index == this.length){
            // append to the end of the list
            this.addLast(obj);
        }else if(index == 0){
            // insert to the beginning of the list
            this.addFirst(obj);
        }else{
            // insert on the specified position
            Node<T> newNode = new Node<T>(obj);
            Node<T> current = this.first.next;
            Node<T> previous = this.first;
            int currentIndex = 0;
            while(currentIndex != index){

                previous.next = current;
                current = current.next;
                currentIndex++;
            }
            previous.next = newNode;
            newNode.next = current;
            this.length++;
        }
    }

    // Returns the item from the specified position
    public T get(int index) throws IndexOutOfBoundsException{
        if(index < 0 || index >= this.length){
            // the index is invalid, throw an Exception
            throw new IndexOutOfBoundsException("Index: "+ index);
        }else if(index == this.length - 1){
            // returns the last item
            return this.getLast().value;
        }else if(index == 0){
            // returns the first item
            return this.getFirst().value;
        }else{
            // returns the item on the specified position
            Node<T> current = this.first.next;
            int currentIndex = 1;
            while(currentIndex != index){
                current = current.next;
                currentIndex++;
            }
            return current.value;
        }
    }

    // remove the duplicated elements from the list
    public void removeDuplicates(){
        if(this.length >= 2){
            Node<T> current1 = this.first;
            while(current1.next != null){
                Node<T> current2 = current1.next;
                Node<T> current2_previous = current1;
                while(current2 != null){
                    if(((Email) current1.value).equals(current2.value)){

                        if(current2.next != null){
                            current2_previous.next = current2.next;
                        }else{
                            current2_previous.next = null;
                            this.last = current2_previous;
                        }

                        this.length--;
                    }
                    current2_previous = current2;
                    current2 = current2.next;
                }
                current1 = current1.next;
            }
        }
    }

    // Remove all the elements from the list
    public void emptyList(){
        this.first = null;
        this.last = null;
        this.length = 0;
    }

    // Node
    // A simple linked list node
    private class Node<T> implements Serializable {
        private Node next;
        private T value;

        Node(T value){
            this.value = value;
        }
    }

}

