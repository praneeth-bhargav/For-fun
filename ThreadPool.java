package org.example;
 
 //Rename file to Main.java and change package necessarily
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
 
class ConcurrentQueue<T> extends LinkedList<T>{
//    private final LinkedList<T> q=new LinkedList<>();
    @Override
     public boolean add(T t){
        synchronized (this){
            return super.add(t);
        }
    }
    @Override
     public T removeFirst(){
        synchronized (this){
            return super.removeFirst();
        }
    }
    @Override
     public T getFirst(){
        synchronized (this){
            return super.getFirst();
        }
    }
    @Override
     public boolean isEmpty(){
        synchronized (this){
            return super.isEmpty();
        }
    }
}
class ThreadPool{
    int poolSize;
    final Thread[] threads;
 
    private final AtomicBoolean isRunning=new AtomicBoolean(false);
    AtomicInteger running=new AtomicInteger(0);
    private final LinkedList<Runnable> q=new ConcurrentQueue<>();
    public void stop(){
        isRunning.set(false);
    }
    public ThreadPool(int poolSize){
        this.poolSize=poolSize;
        threads=new Thread[poolSize];
        running.set(0);
        isRunning.set(true);
        Thread manager = new Thread(() -> {
            while (isRunning.get()) {
                synchronized (threads) {
                    for (int i = 0; i < threads.length; i++) {
                        Runnable runnable=!q.isEmpty()?q.getFirst():null;
                        if(runnable!=null){
                            if (threads[i] != null && !threads[i].isAlive()) {
                                threads[i]=new Thread(runnable);
                                q.removeFirst();
                                threads[i].start();
                            }else if(threads[i]==null){
                                threads[i]=new Thread(runnable);
                                running.set(running.get()+1);
                                q.removeFirst();
                                threads[i].start();
                            }
                        }else{
                            if (threads[i] != null && !threads[i].isAlive()) {
                                running.set(running.get()-1);
                                threads[i]=null;
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if(running.get()==0 && q.isEmpty()){
                    stop();
                }
            }
        });
        manager.start();
    }
    public void add(Runnable runnable){
        if(running.get()==poolSize){
            q.add(runnable);
        }else if(running.get()<poolSize){
            synchronized (threads){
                for(int i=0;i<threads.length;i++){
                        if(threads[i]==null){
                            threads[i]=new Thread(runnable);
                            running.set(running.get()+1);
                            threads[i].start();
                            break;
                        }
                }
            }
        }else{
            System.out.println("oops");
        }
    }
 
}
public class Main   {
    public static void main(String[] args) throws InterruptedException {
        ThreadPool pool=new ThreadPool(10);
        for(int i=0;i<30;i++){
            int finalI = i;
            pool.add(()->{
                try {
                    Thread.sleep(1000);
                }catch (Exception e){
                    System.out.println("interrupted externally");
                }
                System.out.println("Hi from "+ finalI);
            });
        }
    }
}
