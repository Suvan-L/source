package jdk.concurrent.code;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 【编程题】
 *      开启 3 个线程，三个线程 ID 分别为 A, B, C，
 *      每个线程将自己 ID 在屏幕打印 10 遍，要求输出结果按顺序显示
 *      如： ABCABCABCABC.... 依次递归
 *
 * @author Suvan
 * @date 2018.01.25
 */
public class TestThreadABCAlternateDemo {

    /**
     *
     * @param args
     */
    public static void main(String [] args) {
       AlternateABC alt = new AlternateABC();

       String [] letterArray = {"A", "B", "C"};
       for (int i = 0; i < 3; i++) {
           new Thread(new Runnable() {
               @Override
               public void run () {
                   for (int i = 0; i < 20; i++) {
                       alt.loop();
                   }
               }
           }, letterArray[i]).start();
       }
    }
}
class AlternateABC {

    /**
     * 标识现在正在执行的打印序号（0-A, 1-B, 2-C）
     */
    private int sign = 0;

    private Lock lock = new ReentrantLock();
    private ArrayList<Condition> conditionList = new ArrayList<>(3);

    /**
     * 构造函数声明 3 个线程通讯实例
     *      - 可休眠唤醒 3 个线程进行工作
     */
    public AlternateABC() {
        for (int i = 0; i < 3; i++) {
            conditionList.add(lock.newCondition());
        }
    }

    /**
     * 循环打印
     *      - 同步加锁
     *      - 判断当前应该打印的字母标识（sign 变量，0-A，B-1,C-2）
     *          - 若无法对应，则休眠线程，使其进入等待状态
     *      - 打印线程名（满足 sign 指定的顺序）
     *      - 设置顺序 sign + 1，并唤醒下一个线程
     *      - 同步解锁
     */
    public void loop() {
        lock.lock();

        try {
            String name = Thread.currentThread().getName();
            int currentSign = "A".equals(name) ? 0 : "B".equals(name) ? 1 : 2;
            if (sign != currentSign) {
                conditionList.get(currentSign).await();
            }

            System.out.print(Thread.currentThread().getName());

            sign = sign == 2 ? 0 : ++sign;
            conditionList.get(sign).signal();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
