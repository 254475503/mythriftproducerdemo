package com.xiaoju.shiyifan.client;

import com.xiaoju.shiyifan.GreetingService;

@ThriftClient(client = GreetingService.class)
public interface GreetingThriftService extends ThriftInterfaceHolder<GreetingService.Iface> {
}
