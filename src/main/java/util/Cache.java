package util;

import namespace.Inode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Cache {
    class Node{
        String key;
        Inode value;
        Node next;
        Node prev;
        public Node(String key, Inode value){
            this.key = key;
            this.value = value;
            this.next = null;
            this.prev = null;
        }
    }

    private Map<String, Node> cache;
    private int capacity;
    private Node head;
    private Node end;

    public Cache(int capacity){
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.head = null;
        this.end = null;
    }

    public Inode get(String key){
//        System.out.println("-------Inside cache get-----");
//        System.out.println("Key : " + key);
        Node node = null;
        do{
            if(!cache.containsKey(key)){
                break;
            }
            node = cache.get(key);
            remove(node);
            setHead(node);
        }while(false);
        return node!=null ? node.value : null;
    }

    public void put(String key, Inode value){
//        System.out.println("-------Inside cache put-----");
//        System.out.println("Key : " + key);
        Node node;
        if(cache.containsKey(key)){
            node = cache.get(key);
            node.value = value;
            remove(node);
        }else{
            node = new Node(key, value);
            if(cache.size() >= capacity){
                cache.remove(end.key);
                remove(end);
            }
            cache.put(key, node);
        }
        setHead(node);
    }

    public void delete(String key){
        if(!cache.containsKey(key)){
            return;
        }
        remove(cache.get(key));
        cache.remove(key);
    }

    private void setHead(Node node) {
//        System.out.println("Adding to head");
        node.prev = null;
        if (head != null) {
            head.prev = node;
        }
        node.next = head;
        head = node;
        if(end == null){
            end = node;
        }
    }

    private void remove(Node node){
        if(node.prev != null){
            node.prev.next = node.next;
        }else{
            head = node.next;
        }

        if(node.next != null){
            node.next.prev = node.prev;
        }else{
            end = node.prev;
        }
    }

    public void invalidateKeyPrefix(String prefix){
        Iterator<String> itr = cache.keySet().iterator();

        while(itr.hasNext()) {
            String key = itr.next();
            if(key.contains(prefix)) {
                remove(cache.get(key));
                itr.remove();
            }
        }
//
//        for(Map.Entry<String,Node> entry : cache.entrySet()){
//            if(entry.getKey().contains(prefix)){
//                delete(entry.getKey());
//            }
//        }
    }

    public String toString(){
        StringBuilder str = new StringBuilder();
//        System.out.println("cache size : " + cache.size() + "\t Capacity: " + this.capacity + "\nContents: \n");
        for(Map.Entry<String,Node> entry : cache.entrySet()){
            str.append("[" + entry.getKey() + "]:[" + entry.getValue().value.toString() + "]\n");
        }
        return str.toString();
    }

}
