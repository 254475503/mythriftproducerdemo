package com.xiaoju.shiyifan.client;

import com.xiaoju.shiyifan.TestService;

@ThriftClient(client = TestService.class)
public interface TestThriftService extends ThriftInterfaceHolder<TestService.Iface> {
}
