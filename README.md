
**jrouter** is an open source lightweight method router processing container implemented entirely in Java. It focuses on methods' mapping, invoking, intercepting and result processing. You can use it to search and collect your application objects' methods for HTTP controller, Web services, RPC, a variety of applications, etc.

**jrouter** 是一个基于对象方法架构的开源轻量级Java容器。它专注于方法的映射、调用、拦截和结果处理，采用基于配置和注解的方式来抽取和收集程序中对象的方法（method）以用于HTTP控制器，Web服务，RPC，各种应用等。

针对方法，提供基于注解(@Annotation)的配置：

★ 命名空间（Namespace）

★ 行为定义（Action）

★ 拦截器（Interceptor）

★ 拦截栈（InterceptorStack）

★ 结果对象（Result）

★ 结果类型（ResultType）

![outline](http://git.oschina.net/sundancer/jrouter/raw/master/outline.png)

● require [jdk 1.6+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

● require [slf4j](http://www.slf4j.org/download.html)

◇ [changelog](http://git.oschina.net/sundancer/jrouter/blob/master/src/main/resources/changelog.txt)

Adding to your Maven2/3 application:

    <repositories>
        ...
        <repository>
            <id>inn4j-repo</id>
            <name>inn4j-repository</name>
            <url>https://raw.githubusercontent.com/sundancer/inn4j-repository/master/repo/</url>
        </repository>
    </repositories>

    <dependencies>
        ...
        <dependency>
            <groupId>jrouter</groupId>
            <artifactId>jrouter</artifactId>
            <version>1.7.0</version>
        </dependency>
    </dependencies>
Springframework Integration:

    <!-- JRouter ActionFactory -->
    <bean id="actionFactory" class="jrouter.spring.DefaultActionFactoryBean">
        <!-- optional default:null -->
        <property name="configLocation" value="classpath:jrouter.xml" />
        <!-- optional default -->
        <property name="actionFactoryClass" value="jrouter.impl.DefaultActionFactory"/>
        <!-- optional default -->
        <property name="objectFactory">
            <bean class="jrouter.spring.SpringObjectFactory"/>
        </property>
        <!-- optional default -->
        <property name="actionFactoryProperties">
            <value>
                <!-- optional default:null deprecated since 1.6.6 -->
                <!--actionInvocationClass = jrouter.impl.DefaultActionInvocation-->
                <!-- optional default:null -->
                defaultInterceptorStack = empty
                <!-- optional default:null -->
                defaultResultType = empty
                <!-- optional default -->
                pathSeparator = /
                <!-- optional default -->
                extension = .
                <!-- optional default -->
                actionCacheNumber = 10000
                <!-- optional default -->
                bytecode = javassist
                <!-- optional default -->
                converterFactory = jrouter.impl.MultiParameterConverterFactory
            </value>
        </property>

        <!-- scan classes properties -->
        <property name="componentClassScanProperties">
            <list>
                <value>
                    <!-- required -->
                    package = jrouter
                    <!-- optional, if empty means all -->
                    includeExpression = jrouter.impl.**
                    <!-- optional -->
                    excludeExpression = jrouter.result.**, jrouter.interceptor.**
                </value>
            </list>
        </property>

        <property name="interceptors">
            <list>
                <!-- the value can be the class name or the ref bean: -->
                <value>jrouter.interceptor.SampleInterceptor</value>
            </list>
        </property>
        <property name="interceptorStacks">
            <list>
                <!-- the value can be the class name or the ref bean: -->
                <value>jrouter.interceptor.DefaultInterceptorStack</value>
            </list>
        </property>
        <property name="resultTypes">
            <list>
                <!-- the value can be the class name or the ref bean: -->
                <value>jrouter.result.DefaultResult</value>
            </list>
        </property>
        <property name="results">
            <list>
                <!-- the value can be the class name or the ref bean: -->
                <value>jrouter.result.DefaultResult</value>
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
                <bean class="jrouter.spring.AopActionBean">
                    <property name="matches" value="/**"/>
                    <property name="interceptorStackNames" value="empty"/>
                    <property name="interceptorNames" value=""/>
                    <property name="typeName" value="add-before"/>
                </bean>
            </list>
        </property>
    </bean>