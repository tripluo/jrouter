
**jrouter** is an open source lightweight method router processing container implemented entirely in Java. It focuses on methods' mapping, invoking, intercepting and result processing. You can use it to search and collect your application objects' methods for HTTP controller, Web services, RPC, a variety of applications, etc.

**jrouter** 是一个围绕对象方法基于责任链（拦截器）模式设计的开源轻量级Java容器。专注于方法的映射、调用、拦截和结果处理，采用基于配置和注解的方式来抽取和收集程序中对象的方法（method）以用于路由映射， HTTP控制器，RPC，各种应用等。

针对方法，提供基于注解(@Annotation)的配置：

★ 命名空间（Namespace）

★ 行为定义（Action）

★ 拦截器（Interceptor）

★ 拦截栈（InterceptorStack）

★ 结果对象（Result）

★ 结果类型（ResultType）

![outline](https://raw.githubusercontent.com/tripluo/jrouter/master/outline.png)

● require [jdk 1.7+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

● require [slf4j](http://www.slf4j.org/download.html)

◇ [changelog](https://github.com/tripluo/jrouter/blob/master/src/main/resources/changelog.txt)

### Maven: ###
```xml
<dependency>
    <groupId>net.jrouter</groupId>
    <artifactId>jrouter</artifactId>
    <version>1.8.3</version>
</dependency>
```

###  JavaConfig: ###
```
import net.jrouter.ActionFactory;
import net.jrouter.bytecode.javassist.JavassistMethodChecker;
import net.jrouter.impl.PathActionFactory;
import net.jrouter.spring.SpringObjectFactory;
import net.jrouter.util.ClassScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
...
    @Bean
    ActionFactory<String> actionFactory(ApplicationContext applicationContext) {
        PathActionFactory.Properties properties = new PathActionFactory.Properties();
        //default:10000
        properties.setActionCacheNumber(10000);
        //default:null
        properties.setDefaultResultType("empty");
        //default:null
        properties.setDefaultInterceptorStack("empty");
        //default:null
        properties.setExtension(".");
        //default:/
        properties.setPathSeparator('/');
        //default:PathActionFactory.DefaultActionFilter
        properties.setActionFilter(...);
        //default:MultiParameterConverterFactory
        properties.setConverterFactory(...);
        //default:JavassistMethodInvokerFactory
        properties.setMethodInvokerFactory(...);
        //default:PathActionFactory.DefaultObjectFactory
        properties.setObjectFactory(new SpringObjectFactory(applicationContext));
        //default:null
        properties.setMethodChecker(new JavassistMethodChecker("net.jrouter.ActionInvocation.invoke(**)|net.jrouter.ActionInvocation.invokeActionOnly(**)"));
        //default:PathActionFactory.StringPathGenerator
        properties.setPathGenerator(...);

        PathActionFactory actionFactory = new PathActionFactory(properties);
        //add interceptors
        actionFactory.addInterceptors(net.jrouter.interceptor.SampleInterceptor.class);
        //add interceptorStacks
        actionFactory.addInterceptorStacks(net.jrouter.interceptor.DefaultInterceptorStack.class);
        //add resultTypes
        actionFactory.addResultTypes(net.jrouter.result.DefaultResult.class);
        //add results
        actionFactory.addResults(net.jrouter.result.DefaultResult.class);

        ClassScanner scanner = new ClassScanner();
        scanner.setIncludePackages(new HashSet<>(Arrays.asList("net.jrouter")));
        scanner.setIncludeExpressions(new HashSet<>(Arrays.asList("net.jrouter.impl.**")));
        scanner.setExcludeExpressions(new HashSet<>(Arrays.asList("net.jrouter.result.**", "net.jrouter.interceptor.**")));
        //add actions
        for (Class<?> cls : scanner.getClasses()) {
            actionFactory.addActions(cls);
        }
        return actionFactory;
    }
```

### Springframework Integration: ###
```xml
<!-- JRouter ActionFactory -->
<bean id="actionFactory" class="net.jrouter.spring.DefaultActionFactoryBean">
    <!-- optional default:null -->
    <property name="configLocation" value="classpath:jrouter.xml" />
    <!-- optional default -->
    <property name="actionFactoryClass" value="net.jrouter.impl.PathActionFactory"/>
    <!-- optional default -->
    <property name="objectFactory">
        <bean class="net.jrouter.spring.SpringObjectFactory"/>
    </property>
    <!-- optional default -->
    <property name="actionFactoryProperties">
        <value>
            <!-- optional default:null -->
            defaultInterceptorStack = empty
            <!-- optional default:null -->
            defaultResultType = empty
            <!-- optional default -->
            pathSeparator = /
            <!-- optional default -->
            pathGenerator = net.jrouter.impl.PathActionFactory$StringPathGenerator
            <!-- optional default:null -->
            extension =
            <!-- optional default -->
            actionCacheNumber = 10000
            <!-- optional default -->
            bytecode = javassist
            <!-- optional default -->
            converterFactory = net.jrouter.impl.MultiParameterConverterFactory
            <!-- optional default:null -->
            interceptorMethodChecker = net.jrouter.ActionInvocation.invoke(**)|net.jrouter.ActionInvocation.invokeActionOnly(**)
            <!-- optional default:null -->
            actionFilter =
            <!-- optional default:PathActionFactory.$ -->
            pathGenerator =
        </value>
    </property>

    <!-- scan classes properties -->
    <property name="componentClassScanProperties">
        <list>
            <value>
                <!-- required -->
                package = net.jrouter
                <!-- optional, if empty means all -->
                includeExpression = net.jrouter.impl.**
                <!-- optional -->
                excludeExpression = net.jrouter.result.**, net.jrouter.interceptor.**
            </value>
        </list>
    </property>

    <property name="interceptors">
        <list>
            <!-- the value can be the class name or the ref bean: -->
            <value>net.jrouter.interceptor.SampleInterceptor</value>
        </list>
    </property>
    <property name="interceptorStacks">
        <list>
            <!-- the value can be the class name or the ref bean: -->
            <value>net.jrouter.interceptor.DefaultInterceptorStack</value>
        </list>
    </property>
    <property name="resultTypes">
        <list>
            <!-- the value can be the class name or the ref bean: -->
            <value>net.jrouter.result.DefaultResult</value>
        </list>
    </property>
    <property name="results">
        <list>
            <!-- the value can be the class name or the ref bean: -->
            <value>net.jrouter.result.DefaultResult</value>
        </list>
    </property>
    <property name="actions">
        <list>
            <!-- the value can be the class name or the ref bean: -->
        </list>
    </property>
    <property name="componentBeanScanProperties">
        <value>
            <!-- required -->
            includeComponentBeanExpression =
            <!-- optional -->
            excludeComponentBeanExpression =
            <!-- optional -->
            includeComponentClassExpression =
            <!-- optional -->
            excludeComponentClassExpression =
        </value>
    </property>

    <property name="aopActions">
        <list>
            <bean class="net.jrouter.spring.AopActionBean">
                <property name="matches" value="/**"/>
                <property name="interceptorStackNames" value="empty"/>
                <property name="interceptorNames" value=""/>
                <property name="typeName" value="add-before"/>
            </bean>
        </list>
    </property>
</bean>
```