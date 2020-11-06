线程
1. 进程和线程
2. 线程的状态
3. 线程调度策略（抢占式，时间片轮转）
4. 使用方式（Thread、Runnable、Future）
5. 多线程（优缺点）
6. 线程安全，同步
    1. volatile 成员变量使用，保证下一个读取操作在前一个写操作之后
    2. 锁
    3. Object.wait() / notify() / notifyAll() （同步块内部使用，否则 IllegalMonitorStateException）
    4. Thread. static sleep(long) 终止 / interrupt() / static yield()放弃让出 / join() / ~~suspend~~ / ~~resume~~ / ~~stop~~
    5. ThreadLocal
7. 线程池
