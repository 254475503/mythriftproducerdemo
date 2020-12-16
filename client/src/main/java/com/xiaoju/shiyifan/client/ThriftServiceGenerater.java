package com.xiaoju.shiyifan.client;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Set;

/*
    使用beandefinitionregistry后置拓展点，向ioc容器中注册项目中所有的标记了thriftclient注解的thrfitclient代理类。
 */
@Component
public class ThriftServiceGenerater implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, BeanClassLoaderAware, ResourceLoaderAware {
    private Environment environment;
    private ClassLoader classLoader;
    private ResourceLoader resourceLoader;
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        //扫描指定路径下的所有标记了thriftclient的接口
            Set<BeanDefinition> beanDefinitions = doScan("com.xiaoju.shiyifan.client",ThriftClient.class);
            //循环处理
            for(BeanDefinition beanDefinition : beanDefinitions){
                //其实有点没必要，因为全部都是注解了的beandefinition
                if(beanDefinition instanceof AnnotatedBeanDefinition) {

                    AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
                    GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
                    try {
                        //获取接口名
                        Class<?> targetClass = Class.forName(annotatedBeanDefinition.getMetadata().getClassName());
                        //以目标类的simplename首字母小写作为beanname（标准springbbean名称规范）
                        String beanName = lowerCaseFirstCharacter(targetClass.getSimpleName());

                        //获取这个类的所有接口，将接口中的泛型拿出来，把泛型拿出来以后，根据泛型的名称，拼出需要代理的thriftclient接口名字并且拿到class文件，进行创建代理类
                        Type[] interfaces = targetClass.getGenericInterfaces();
                        Class<?> proxyInterface = null;
                        for(Type type : interfaces){
                            if (!(type instanceof ParameterizedType)) {
                                continue;
                            }
                            ParameterizedType pt = (ParameterizedType) type;
                            Class arg = (Class) pt.getActualTypeArguments()[0];
                            if (arg.getName().endsWith("$Iface")) {
                                proxyInterface = arg;
                            }
                        }
                        //根据拼出来的名字，如GreetingService$Iface生成代理类
                        Class<?> proxyClass = Proxy.getProxyClass(classLoader,proxyInterface);

                        genericBeanDefinition.setBeanClass(proxyClass);

                        genericBeanDefinition.setBeanClassName(proxyClass.getName());
                        //创建构造参数
                        ConstructorArgumentValues args = new ConstructorArgumentValues();
                        //将invocationhandler作为构造参数
                        args.addGenericArgumentValue(new ThriftClientInvocationHandler(targetClass));
                        genericBeanDefinition.setConstructorArgumentValues(args);
                        //注册bean
                        beanDefinitionRegistry.registerBeanDefinition(beanName,genericBeanDefinition);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }
    }

    public String lowerCaseFirstCharacter(String originStr) {
        char[] charArray = originStr.toCharArray();
        charArray[0]+=32;
        return String.valueOf(charArray);
    }
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    public Set<BeanDefinition> doScan(String classPath,Class<? extends Annotation> annotationClass){
        //使用spring提供的扫描器
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false,environment){
            //重写iscandidate方法，判断如果是标记了注解的接口 加入返回列表
        @Override
        protected boolean isCandidateComponent(
                AnnotatedBeanDefinition beanDefinition) {
            if (beanDefinition.getMetadata().isInterface()) {
                try {
                    Class<?> target = ClassUtils.forName(
                            beanDefinition.getMetadata().getClassName(),
                            classLoader);
                    return !target.isAnnotation();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return false;
        }
    };
        //扫描器内置的过滤器，过滤出标记了目标注解的类。如果不重写iscandiadate方法，就只会返回接口的实现类。重写了方法后实现了返回接口
        scanner.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
        //不知道啥用。
        scanner.setResourceLoader(resourceLoader);

        //扫描方法
        return scanner.findCandidateComponents(classPath);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader  = classLoader;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
