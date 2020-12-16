package com.xiaoju.shiyifan.service;

import com.xiaoju.shiyifan.TestService;
import org.apache.thrift.TException;

public class TestServiceImpl implements TestService.Iface{

    @Override
    public String test() throws TException {
        return "test nmb";
    }
}
