package com.nowcoder.community;


import com.nowcoder.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTest {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter(){
        String str = "防守打法阿萨德水电费赌$##博，嫖@@!娼，啊实打实，吸##@毒";
        str = sensitiveFilter.Filter(str);
        System.out.println(str);
    }
}
