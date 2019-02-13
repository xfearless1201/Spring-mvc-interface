package payTest;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cn.tianxia.dao.NotifyDao;
import com.cn.tianxia.entity.RechargeVO;
import com.cn.tianxia.pay.utils.RandomUtils;

/**
 * 
 * @ClassName QueueTest
 * @Description 队列测试类
 * @author Hardy
 * @Date 2018年11月12日 上午11:24:38
 * @version 1.0.0
 */
public class QueueTest{

//    @Test
    public void concurrentLinkedQueueTest(){
        ConcurrentLinkedQueue<String> concurrentLinkedQueue = new ConcurrentLinkedQueue<String>();    
        concurrentLinkedQueue.add("a");    
        concurrentLinkedQueue.add("b");    
        concurrentLinkedQueue.add("c");    
        concurrentLinkedQueue.offer("d"); // 将指定元素插入到此队列的尾部。    
        concurrentLinkedQueue.peek(); // 检索并移除此队列的头，如果此队列为空，则返回 null。    
        concurrentLinkedQueue.poll(); // 检索并移除此队列的头，如果此队列为空，则返回 null。    
    
        for (String str : concurrentLinkedQueue) {    
            System.out.println(str);    
        }    
    }
    
    
//    @Test
    public void addOrder()throws Exception{
        int peopleNum = 10000;//一万单
        int tableNum = 10;//十个线程
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        CountDownLatch count = new CountDownLatch(tableNum);//计数器
        for (int i = 0; i < peopleNum; i++) {
            queue.offer("订单数_" + i);
        }
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");
        NotifyDao notifyDao = context.getBean(NotifyDao.class);
        //执行10个线程从队列取出元素（10个桌子开始供饭）
        System.out.println("-----------------------------------开始创建订单-----------------------------------");
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(tableNum);
        for(int i=0;i<tableNum;i++) {
            executorService.submit(new addRecharge(queue,notifyDao,count,"00" + (i+1)));
        }
        //计数器等待，知道队列为空（所有人吃完）
        count.await();
        long time = System.currentTimeMillis() - start;
        System.out.println("-----------------------------------所有订单创建完成-----------------------------------");
        System.out.println("共耗时：" + time);
        //停止线程池
        executorService.shutdown();
    }
    
    
//    @Test
    public void queueTest(){
        int requestNum = 100000;//十万个请求数
        //创建队列对象
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        for(int i=0; i<requestNum; i++){
            queue.offer("请求编号_"+i);
        }
        while(!queue.isEmpty()){
            System.err.println("完成编号_"+queue.poll());
        }
        System.err.println("执行完成!");
    }
    
//    @Test
    public void inserOrder(){
        RechargeVO rechargeVO = new RechargeVO();
        rechargeVO.setCagent("BL1");
        rechargeVO.setCid(2);
        rechargeVO.setIp("127.0.0.1");
        String currtime = String.valueOf(System.currentTimeMillis());
        String randomStr = RandomUtils.generateString(8);
        String orderNo = currtime + randomStr;
        rechargeVO.setOrderNo(orderNo);
        rechargeVO.setPayCode("ali");
        rechargeVO.setPayId(532);
        rechargeVO.setOrderAmount(100.00);
        rechargeVO.setPayType("3");
        rechargeVO.setPaymentName("TESTPAY");
        rechargeVO.setPayStatus("0");
        rechargeVO.setUid(526829);
        rechargeVO.setTradeStatus("0");
//        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");
//        NotifyDao notifyDao = context.getBean(NotifyDao.class);
//        notifyDao.insertRechrageOrder(rechargeVO);
    }

    
    /**
     * 
     * @ClassName addRecharge
     * @Description 私有 内部类
     * @author Hardy
     * @Date 2018年11月12日 上午11:28:53
     * @version 1.0.0
     */
    private static class addRecharge implements Runnable{
        
        private ConcurrentLinkedQueue<String> queue;
        
        private NotifyDao notifyDao;

        private CountDownLatch count;
        
        private String name;

        public addRecharge(ConcurrentLinkedQueue<String> queue, NotifyDao notifyDao, CountDownLatch count,
                String name) {
            super();
            this.queue = queue;
            this.notifyDao = notifyDao;
            this.count = count;
            this.name = name;
        }





        @Override
        public void run() {
            while (!queue.isEmpty()) {
                RechargeVO rechargeVO = new RechargeVO();
                rechargeVO.setIp("127.0.0.1");
                String currtime = String.valueOf(System.currentTimeMillis());
                String randomStr = RandomUtils.generateString(8);
                String orderNo = currtime + randomStr;
                rechargeVO.setOrderNo(orderNo);
                rechargeVO.setUid(526829);
//                notifyDao.insertRechrageOrder(rechargeVO);
                //从队列取出一个元素 排队的人少一个
                System.out.println("【" +queue.poll() + "】----订单创建完毕...， 工作编号："+name);
            }
            count.countDown();//计数器-1
        }
     
    }
    
    @Test
    public void regexTest(){
        
        String regex = "^[\u4e00-\u9fa5]{2,5}$";
        
        String str = "中过忍忍你号";
        
        if(str.matches(regex)){
            System.err.println("合法名称");
        }else{
            System.err.println("输入名称不合法，请重新输入");
        }
    }
    
}
