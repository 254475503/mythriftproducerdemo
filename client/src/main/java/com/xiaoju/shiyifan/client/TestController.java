package com.xiaoju.shiyifan.client;

import com.xiaoju.shiyifan.GreetingService;
import com.xiaoju.shiyifan.TestService;
import com.xiaoju.shiyifan.mytype;
import org.apache.thrift.TException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Controller
@RestController("/asd")
public class TestController {
    @Resource
    TestService.Iface testThriftService;
    @Resource
    GreetingService.Iface greetingService;

    @RequestMapping("/test")
    public void test() throws TException {
        System.out.println(testThriftService.test());
        mytype mytyp = new mytype();
        mytyp.setId(new Short("1"));
        mytyp.setName("asd");
        System.out.println(greetingService.sayHello(mytyp));

    }
}
