package com.xiaoju.shiyifan.service;

import com.xiaoju.shiyifan.GreetingService;
import com.xiaoju.shiyifan.mytype;
import org.apache.thrift.TException;

public class ServiceImpl implements GreetingService.Iface {
    @Override
    public String sayHello(mytype info) throws TException {
        return "huang nm chouhai hello";
    }
}
