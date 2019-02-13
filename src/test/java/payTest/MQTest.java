package payTest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cn.tianxia.mq.producer.IProducerService;
import com.cn.tianxia.mq.vo.GameTransferVO;

public class MQTest {
    
    @Autowired
    private IProducerService producerService;

    @Before
    public void befor() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring-rocketmq-producer.xml");
        producerService = (IProducerService) context.getBean("producer");
    }
    
    @Test
    public void testSendMq() {
        GameTransferVO gameTransferVO= new GameTransferVO();
        gameTransferVO.setBiilno("TX14978667771591");
        gameTransferVO.setUid("1195");
        gameTransferVO.setId(79);
        producerService.send("TEST_TOPIC", gameTransferVO);
    }
}
