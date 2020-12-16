package com.xiaoju.shiyifan.server;

import com.xiaoju.shiyifan.GreetingService;
import com.xiaoju.shiyifan.TestService;
import com.xiaoju.shiyifan.service.ServiceImpl;
import com.xiaoju.shiyifan.service.TestServiceImpl;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

public class Multiserver {
    public static void main(String[] args) throws TTransportException {
        TServerSocket serverSocket = new TServerSocket(9090);
        TMultiplexedProcessor processor = new TMultiplexedProcessor();
        GreetingService.Processor<ServiceImpl> greetingServiceProcessor = new GreetingService.Processor<ServiceImpl>(new ServiceImpl());
        TestService.Processor<TestServiceImpl> testServiceProcessor = new TestService.Processor<TestServiceImpl>(new TestServiceImpl());
        processor.registerProcessor("GreetingService",greetingServiceProcessor);
        processor.registerProcessor("TestService",testServiceProcessor);
        TServer tserver = new TThreadPoolServer(new TThreadPoolServer.Args(serverSocket).processor(processor));
        tserver.serve();
    }
}
