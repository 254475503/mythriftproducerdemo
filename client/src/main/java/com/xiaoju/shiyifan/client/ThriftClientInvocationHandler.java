package com.xiaoju.shiyifan.client;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 代理thriftclient的invocationhandler
 */
public class ThriftClientInvocationHandler implements InvocationHandler {
    private Class<?> targetClass ;


    public ThriftClientInvocationHandler(Class<?> clazz){
        targetClass = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Annotation[] annotations = targetClass.getAnnotations();
        //拿到目标类的ThriftClient注解，看他到底是对应哪一个thrift服务
        ThriftClient thriftClient = null;
        for(Annotation annotation : annotations){
            if(annotation instanceof ThriftClient)
                thriftClient = (ThriftClient) annotation;
        }
        //根据thrft服务的类名，评出client的类名 如GreetingService$Client
        Class<?> client = Class.forName(thriftClient.client().getName()+"$Client");

        //获取client的构造函数
        Constructor<?> constructor = client.getConstructor(TProtocol.class);
        TTransport transport = new TSocket("localhost",9090);

        TProtocol protocol = new TBinaryProtocol(transport);
        TProtocol multiplexProtocol = new TMultiplexedProtocol(protocol,thriftClient.client().getSimpleName());
        //使用构造函数构造出这个service对应的client
        Object realclient = constructor.newInstance(multiplexProtocol);
        transport.open();
        //使用构造出来的client发起rpc调用。
        return method.invoke(realclient,args);
    }
}
