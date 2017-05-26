package com.np.ioc;

/** 注解处理器动态生成的代理必须实现该接口. */
public interface Injector<T> {
    void inject(T host, Object view);
}
