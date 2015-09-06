/*
 * Copyright (C) 2010-2111 sunjumper@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package jrouter.config;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jrouter.ActionFactory;
import jrouter.config.AopAction.Type;
import jrouter.impl.DefaultActionFactory;
import jrouter.impl.DefaultActionProxy;
import jrouter.impl.Injector;
import jrouter.impl.InterceptorProxy;
import jrouter.util.AntPathMatcher;
import jrouter.util.ClassUtil;
import jrouter.util.CollectionUtil;
import jrouter.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 启动jrouter容器的入口配置类。
 * 通过Configuration类加载jrouter的配置文件（默认为jrouter.xml）初始化ActionFactory及加载相应的属性配置，最终得到ActionFactory具体实例。
 *
 * <p>
 * 如果jrouter.xml中未指明ActionFactory的具体实现类，则默认使用{@link DefaultActionFactory
 * }。
 * </p>
 *
 * <p>
 * 通常如下使用：
 * <code><blockquote><pre>
 * Configuration config = new Configuration().load(URL url);
 * ActionFactory factory = config.buildActionFactory();
 * factory...
 * </pre></blockquote></code>
 * </p>
 *
 * @see #buildActionFactory()
 */
public class Configuration implements Serializable {

    private static final long serialVersionUID = 1L;

    /* 日志记录 */
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

////////////////////////////////////////////////////////////////////////////////
//                           xml配置文件元素                                   //
////////////////////////////////////////////////////////////////////////////////
    /**
     * JAXP attribute used to configure the schema language for validation.
     */
    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /** 默认xml文件的名称 */
    public static final String JROUTER_XML = "jrouter.xml";

    /** 默认xsd文件的名称 */
    public static final String JROUTER_XSD = "jrouter-1.6.xsd";

    /** 配置文件中表示ActionFactory的标签名 */
    public static final String ACTION_FACTORY = "action-factory";

    /** 配置文件中表示属性的标签属性 */
    public static final String PROPERTY = "property";

    /** 配置文件中表示类名称的标签属性 */
    public static final String CLASS = "class";

    /** 配置文件中表示值的标签属性 */
    public static final String VALUE = "value";

    /** 配置文件中表示名称的标签属性 */
    public static final String NAME = "name";

    /** 配置文件中表示拦截器的标签名 */
    public static final String INTERCEPTOR = "interceptor";

    /** 配置文件中表示拦截栈的标签名 */
    public static final String INTERCEPTOR_STACK = "interceptor-stack";

    /** 配置文件中表示结果类型的标签名 */
    public static final String RESULT_TYPE = "result-type";

    /** 配置文件中表示结果对象的标签名 */
    public static final String RESULT = "result";

    /** 配置文件中表示Action的标签名 */
    public static final String ACTION = "action";

    /** 配置文件中表示path的标签名 */
    public static final String PATH = "path";

    /** 配置文件中表示包含其它配置的标签名 */
    public static final String INCLUDE = "include";

    /**
     * 配置文件中表示文件名称的标签属性 */
    public static final String FILE = "file";

    /** 配置文件中表示扫描组件的标签名 */
    public static final String COMPONENT_SCAN = "component-scan";

    /** 配置文件中表示包名称的标签属性 */
    public static final String PACKAGE = "package";

    /**
     * 配置文件中表示不包含的标签属性
     *
     * @deprecated
     */
    @Deprecated
    public static final String EXCLUDE = "exclude";

    /** 配置文件中表示包含表达式的标签属性 */
    public static final String INCLUDE_EXPRESSION = "includeExpression";

    /** 配置文件中表示排除表达式的标签属性 */
    public static final String EXCLUDE_EXPRESSION = "excludeExpression";

    /** 配置文件中表示aop配置的标签名 */
    public static final String AOP_CONFIG = "aop-config";

    /** 配置文件中表示针对action的aop配置标签属性 */
    public static final String AOP_ACTION = "aop-action";

    /** 配置文件中表示路径匹配的标签属性 */
    public static final String MATCHES = "matches";

    /** 配置文件中表示类型的标签属性 */
    public static final String TYPE = "type";

    /** 配置文件中表示拦截栈集合的标签属性 */
    public static final String INTERCEPTOR_STACKS = "interceptor-stacks";

    /** 配置文件中表示拦截器集合的标签属性 */
    public static final String INTERCEPTORS = "interceptors";

////////////////////////////////////////////////////////////////////////////////////////////////////

    /* ActionFactory的类型，默热为DefaultActionFactory类型 */
    private Class<? extends ActionFactory> actionFactoryClass = DefaultActionFactory.class;

    /** ActionFactory的属性 */
    private Map<String, Object> actionFactoryProperties;

    /** 组件扫描 */
    private List<ClassScanner> classScanners;

    /** interceptors' class or object */
    private Set<Object> interceptors;

    /** interceptors' properties */
    private Map<Class<?>, Map<String, Object>> interceptorProperties;

    /** interceptorStacks' class or object */
    private Set<Object> interceptorStacks;

    /** interceptorStacks' properties */
    private Map<Class<?>, Map<String, Object>> interceptorStackProperties;

    /** resultTypes' class or object */
    private Set<Object> resultTypes;

    /** resultTypes' properties */
    private Map<Class<?>, Map<String, Object>> resultTypeProperties;

    /** results' class or object */
    private Set<Object> results;

    /** results' properties */
    private Map<Class<?>, Map<String, Object>> resultProperties;

    /** actions' class or object */
    private Set<Object> actions;

    /** actions' properties */
    private Map<Class<?>, Map<String, Object>> actionProperties;

    /** path - action class map */
    private Map<String, Class<?>> pathActions;

    /** path actions' properties */
    private Map<String, Map<String, Object>> pathProperties;

    /** actions' aop */
    private List<AopAction> aopActions;

    /**
     * Constructor with initialization.
     */
    public Configuration() {
        reset();
    }

    /**
     * initiate or reset the collections.
     */
    protected void reset() {
        actionFactoryProperties = new LinkedHashMap<String, Object>();
        interceptors = new LinkedHashSet<Object>();
        interceptorProperties = new LinkedHashMap<Class<?>, Map<String, Object>>();
        interceptorStacks = new LinkedHashSet<Object>();
        interceptorStackProperties = new LinkedHashMap<Class<?>, Map<String, Object>>();
        resultTypes = new LinkedHashSet<Object>();
        resultTypeProperties = new LinkedHashMap<Class<?>, Map<String, Object>>();
        results = new LinkedHashSet<Object>();
        resultProperties = new LinkedHashMap<Class<?>, Map<String, Object>>();
        actions = new LinkedHashSet<Object>();
        actionProperties = new LinkedHashMap<Class<?>, Map<String, Object>>();
        pathActions = new LinkedHashMap<String, Class<?>>();
        pathProperties = new LinkedHashMap<String, Map<String, Object>>();
        classScanners = new ArrayList<ClassScanner>();
        aopActions = new ArrayList<AopAction>();
    }

    /**
     * 加载默认配置文件{@link #JROUTER_XML}。
     *
     * @return 此配置对象的引用。
     *
     * @throws ConfigurationException 如果发生配置错误。
     */
    public Configuration load() throws ConfigurationException {
        return load(JROUTER_XML);
    }

    /**
     * 从指定的URL对象加载配置。
     *
     * @param url 指定的URL。
     *
     * @return 此配置对象的引用。
     *
     * @throws ConfigurationException 如果发生配置错误。
     */
    public Configuration load(URL url) throws ConfigurationException {
        LOG.info("Configuring from url : " + url.toString());
        try {
            return load(url.openStream(), url.toString());
        } catch (IOException ioe) {
            throw new ConfigurationException("Could not configure from URL : " + url, ioe);
        }
    }

    /**
     * 从指定的资源路径加载配置。
     *
     * @param resource 指定的资源路径。
     *
     * @return 此配置对象的引用。
     *
     * @throws ConfigurationException 如果发生配置错误。
     */
    public Configuration load(String resource) throws ConfigurationException {
        LOG.info("Configuration from resource : " + resource);
        return load(getResource(resource));
    }

    /**
     * 从指定的配置文件加载配置。
     *
     * @param configFile 指定的配置文件。
     *
     * @return 此配置对象的引用。
     *
     * @throws ConfigurationException 如果发生配置错误。
     */
    public Configuration load(File configFile) throws ConfigurationException {
        LOG.info("Configuring from file : " + configFile.getName());
        try {
            return load(new FileInputStream(configFile), configFile.toString());
        } catch (FileNotFoundException fnfe) {
            throw new ConfigurationException("Could not find file : " + configFile, fnfe);
        }
    }

    /**
     * 从指定资源获取URL。
     *
     * @param resource 资源文件名。
     *
     * @return URL。
     */
    private static URL getResource(String resource) {
        String name = resource.startsWith("/") ? resource.substring(1) : resource;
        URL url = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            url = classLoader.getResource(name);
        }
        if (url == null) {
            url = Configuration.class.getResource(resource);
        }
        if (url == null) {
            url = Configuration.class.getClassLoader().getResource(name);
        }
        if (url == null) {
            throw new IllegalArgumentException(resource + " not found");
        }
        return url;
    }

    /**
     * 打印分隔符。
     *
     * @param bool 是否打印。
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private static void printSeparator(boolean bool) {
        if (bool)
            System.out.println("--------------------------------------------------------------------------------");
    }

    /**
     * 解析xml配置文件。
     */
    private static class DocumentLoader {

        /* SAX 错误处理对象 */
        static ErrorHandler ERROR_HANDLER = new ErrorHandler() {

            @Override
            public void warning(SAXParseException ex) throws SAXException {
                LOG.warn("Ignored XML validation warning", ex);
            }

            @Override
            public void error(SAXParseException ex) throws SAXException {
                throw ex;
            }

            @Override
            public void fatalError(SAXParseException ex) throws SAXException {
                throw ex;
            }
        };

        /* 用于解析实体的对象 */
        static EntityResolver ENTITY_RESOLVER = new EntityResolver() {

            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws
                    SAXException, IOException {
                if (systemId != null) {
                    InputSource source = new InputSource(getResource(JROUTER_XSD).openStream());
                    source.setPublicId(publicId);
                    source.setSystemId(JROUTER_XSD);
                    return source;
                }
                return null;
            }
        };

        /**
         * 将给定 InputStream 的内容解析为一个 XML 文档，并且返回一个新的 DOM <code>Document</code> 对象。
         *
         * @param stream 包含要解析内容的 InputStream。
         *
         * @return <code>Document</code> 对象。
         */
        private static Document loadDocument(InputStream stream) throws ParserConfigurationException,
                SAXException, IOException {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            try {
                factory.setAttribute(JAXP_SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);
            } catch (IllegalArgumentException ex) {
                // Happens if the parser does not support JAXP 1.2
                throw new ConfigurationException(
                        "Unable to validate using XSD: Your JAXP provider [" + factory
                        + "] does not support XML Schema. Are you running on Java 1.4 with Apache Crimson? "
                        + "Upgrade to Apache Xerces (or Java 1.5) for full XSD support.", ex);
            }
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            docBuilder.setEntityResolver(ENTITY_RESOLVER);
            docBuilder.setErrorHandler(ERROR_HANDLER);
            return docBuilder.parse(stream);
        }
    }

    /**
     * 从指定的InputStream对象中加载配置。
     *
     * @param stream 指定的InputStream。
     * @param resourceName InputStream对象的名称。
     *
     * @return 此配置对象的引用。
     *
     * @throws ConfigurationException 如果发生配置错误。
     */
    protected Configuration load(InputStream stream, String resourceName) throws
            ConfigurationException {
        try {
            Document doc = DocumentLoader.loadDocument(stream);
            //root node : <jrouter>
            Element root = doc.getDocumentElement();

            List<Element> list = null;
            int length = 0;

            //action-factory tag
            list = getChildNodesByTagName(root, ACTION_FACTORY);
            if ((length = list.size()) == 1) {
                Element e = list.get(0);
                String cls = e.getAttribute(CLASS);
                if (StringUtil.isNotBlank(cls))
                    actionFactoryClass = (Class<? extends ActionFactory>) ClassUtil.loadClass(cls);
                LOG.info("Configured SessionFactory : " + cls);
                //ActionFactory's properties
                list = getChildNodesByTagName(e, PROPERTY);
                //parse ActionFactory's properties
                actionFactoryProperties = parseProperties(actionFactoryClass, list);
            } else if (length > 1) {
                throw new ConfigurationException("More than one <" + ACTION_FACTORY + "> tag in : " + resourceName, null);
            }

            //parse "<component-scan>"
            parseScanComponentClasses(root);

            printSeparator(!list.isEmpty());

            //依次添加interceptor、interceptorStack、result-type、result、action。
            parseActionFactoryElements(root);

            //include
            list = getChildNodesByTagName(root, INCLUDE);
            //length = list.size();
            Map<String, String> record = new HashMap<String, String>();
            for (Element e : list) {
                //add included files, use a hash set to avoid circular reference
                parseInclude(resourceName, e.getAttribute(FILE), record);
            }

            //parse "<aop-config>"
            parseAop(root);

        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Could not configure from input stream resource : " + resourceName, e);
        }
        return this;
    }

    /**
     * 由<property>标签解析类的注入属性。
     *
     * @param cls 指定的类型。
     * @param propnodes <property>标签集合。
     *
     * @return 可注入属性的映射。
     *
     * @throws IntrospectionException 如果在内省期间发生异常。
     */
    private static Map<String, Object> parseProperties(Class<?> cls, List<Element> propnodes) throws
            IntrospectionException {
        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        for (Element prop : propnodes) {
            String pName = prop.getAttribute(NAME);
            if (null != properties.put(pName, prop.getAttribute(VALUE))) {
                LOG.warn("Override property [{}] value [{}] in " + cls, pName, prop.getAttribute(VALUE));
            }
        }
        return properties;
    }

    /**
     * 添加包含的配置文件"<include>", 用一个集合映射判断并避免循环引用。
     *
     * @param from 源配置文件名称。
     * @param includeFile 被包含的配置文件名称。
     * @param record 指定的被包含/包含文件的映射。
     */
    private void parseInclude(String from, String includeFile, Map<String, String> record) {
        LOG.info("Load included file : " + includeFile);
        printSeparator(true);

        URL include = getResource(includeFile);
        if (include == null) {
            throw new ConfigurationException("Could not included file : " + include, null);
        }
        InputStream stream = null;
        try {
            stream = include.openStream();
        } catch (IOException e) {
            throw new ConfigurationException("IOException occurs in included file : " + include, e);
        }

        //if circular reference
        if (record.containsKey(includeFile)) {
            throw new ConfigurationException("Load circular reference file, "
                    + "[" + includeFile + "] included in [" + from + "] and [" + record.get(includeFile) + "]", null);
        }
        record.put(includeFile, from);

        try {
            Document doc = DocumentLoader.loadDocument(stream);
            Element root = doc.getDocumentElement();

            //add properties
            parseActionFactoryElements(root);

            //add include
            List<Element> list = getChildNodesByTagName(root, INCLUDE);
            for (Element e : list) {
                parseInclude(includeFile, e.getAttribute(FILE), record);
            }

        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Could not load or parse properties from included file : " + include, e);
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                    LOG.error("Fail to close input stream : " + include, e);
                }
        }
    }

    /**
     * 依次添加interceptor、interceptorStack、resultType、result、action。
     *
     * @param root 文档根节点。
     *
     * @throws ClassNotFoundException 如果没有找到具有指定名称的类。
     * @throws IllegalAccessException 如果底层方法不可访问。
     * @throws IntrospectionException 如果在内省期间发生异常。
     * @throws InstantiationException 如果实例化失败。
     */
    private void parseActionFactoryElements(Element root) throws ClassNotFoundException,
            IllegalAccessException, IntrospectionException, InvocationTargetException {

        List<Element> list = null;
        //interceptor
        list = getChildNodesByTagName(root, INTERCEPTOR);
        for (Element e : list) {
            Class<?> cls = ClassUtil.loadClass(e.getAttribute(CLASS));
            LOG.debug("Load Interceptor class : {}", cls);
            //add interceptors
            if (!interceptors.add(cls)) {
                LOG.warn("Duplicate interceptor class : {}, Override the configuration and properties.", cls);
            }
            //set property nodes
            Map<String, Object> props = parseProperties(cls, getChildNodesByTagName(e, PROPERTY));
            if (!props.isEmpty())
                interceptorProperties.put(cls, props);
        }

        printSeparator(!list.isEmpty());

        //interceptor-stack
        list = getChildNodesByTagName(root, INTERCEPTOR_STACK);
        for (Element e : list) {
            Class<?> cls = ClassUtil.loadClass(e.getAttribute(CLASS));
            LOG.debug("Load InterceptorStack class : {}", cls);
            //add interceptor stacks
            if (!interceptorStacks.add(cls)) {
                LOG.warn("Duplicate InterceptorStack class : {}, Override the configuration and properties.", cls);
            }

            //set property nodes
            Map<String, Object> props = parseProperties(cls, getChildNodesByTagName(e, PROPERTY));
            if (!props.isEmpty())
                interceptorStackProperties.put(cls, props);
        }

        printSeparator(!list.isEmpty());

        //result-type
        list = getChildNodesByTagName(root, RESULT_TYPE);
        for (Element e : list) {
            Class<?> cls = ClassUtil.loadClass(e.getAttribute(CLASS));
            LOG.debug("Load ResultType class : {}", cls);
            //add result types
            if (!resultTypes.add(cls)) {
                LOG.warn("Duplicate ResultType class : {}, Override the configuration and properties.", cls);
            }
            //set property nodes
            Map<String, Object> props = parseProperties(cls, getChildNodesByTagName(e, PROPERTY));
            if (!props.isEmpty())
                resultTypeProperties.put(cls, props);
        }

        printSeparator(!list.isEmpty());

        //result
        list = getChildNodesByTagName(root, RESULT);
        for (Element e : list) {
            Class<?> cls = ClassUtil.loadClass(e.getAttribute(CLASS));
            LOG.debug("Load Result class : {}", cls);
            //add results
            if (!results.add(cls)) {
                LOG.warn("Duplicate Result class : {}, Override the configuration and properties.", cls);
            }
            //set property nodes
            Map<String, Object> props = parseProperties(cls, getChildNodesByTagName(e, PROPERTY));
            if (!props.isEmpty())
                resultProperties.put(cls, props);
        }

        printSeparator(!list.isEmpty());

        //action
        list = getChildNodesByTagName(root, ACTION);
        for (Element e : list) {
            //action <property> nodes
            List<Element> propnodes = getChildNodesByTagName(e, PROPERTY);

            Class<?> cls = ClassUtil.loadClass(e.getAttribute(CLASS));
            LOG.debug("Load Action class : {}", cls);
            //add results
            if (!actions.add(cls)) {
                LOG.warn("Duplicate Action class : {}, Override the configuration and properties.", cls);
            }

            //set property nodes
            Map<String, Object> props = parseProperties(cls, propnodes);
            if (!props.isEmpty())
                actionProperties.put(cls, props);

            //<path> nodes
            List<Element> pathnodes = getChildNodesByTagName(e, PATH);
            for (Element path : pathnodes) {
                String pathName = path.getAttribute(NAME);
                //记录path对应的Action
                pathActions.put(pathName, cls);
                LOG.debug("Load path properties : " + pathName);
                //path <property> nodes
                List<Element> pathpropnodes = getChildNodesByTagName(path, PROPERTY);
                Map<String, Object> pathProps = new LinkedHashMap<String, Object>();
                for (Element p : pathpropnodes) {
                    pathProps.put(p.getAttribute(NAME), p.getAttribute(VALUE));
                }
                if (pathProperties.containsKey(pathName)) {
                    LOG.warn("Duplicate Action path configuration [{}], Override the properties.", pathName);
                }
                if (!pathProps.isEmpty()) {
                    pathProperties.put(pathName, pathProps);
                }
            }
        }

        printSeparator(!list.isEmpty());
    }

    /**
     * 解析"<component-scan>"并添加自动检索的类。
     *
     * @param root 文档根节点。
     *
     * @throws ClassNotFoundException 如果无法定位类。
     */
    private void parseScanComponentClasses(Element root) throws ClassNotFoundException {
        List<Element> list = getChildNodesByTagName(root, COMPONENT_SCAN);
        printSeparator(!list.isEmpty());

        for (Element e : list) {
            String pkg = e.getAttribute(PACKAGE);
            String include = e.getAttribute(INCLUDE_EXPRESSION);
            String exclude = e.getAttribute(EXCLUDE_EXPRESSION);
            LOG.info("Parse <component-scan> : [package = {}, includeExpression = {}, excludeExpression = {}]",
                    pkg, include, exclude);
            Map<String, String> props = new HashMap<String, String>(4);
            if (StringUtil.isNotBlank(pkg)) {
                props.put(PACKAGE, pkg);
                if (StringUtil.isNotBlank(include)) {
                    props.put(INCLUDE_EXPRESSION, include);
                }
                if (StringUtil.isNotBlank(exclude)) {
                    props.put(EXCLUDE_EXPRESSION, exclude);
                }
                //add a new ClassScanner for each <component-scan>
                classScanners.add(parsecComponentClassScanner(props));
            } else {
                LOG.warn("Property [{}] can't be empty for <component-scan>.", PACKAGE);
            }
        }
    }

    /**
     * 由属性映射集合创建一个扫描工具类。
     *
     * @param scannerProperties 扫描工具类的属性映射集合。
     *
     * @return 扫描工具类。
     */
    private ClassScanner parsecComponentClassScanner(Map<String, String> scannerProperties) {
        ClassScanner scanner = new ClassScanner();
        char[] sep = {',', ';'};
        for (Map.Entry<String, String> e : scannerProperties.entrySet()) {
            String name = e.getKey();
            String value = e.getValue();
            if (value == null) {
                LOG.warn("Property [{}] can't be empty.", name);
                continue;
            }
            Set<String> set = new LinkedHashSet<String>();
            if (PACKAGE.equalsIgnoreCase(name)) {
                CollectionUtil.stringToCollection(value, set, sep);
                //packages
                scanner.setIncludePackages(set);
            } else if (INCLUDE_EXPRESSION.equalsIgnoreCase(name)) {
                CollectionUtil.stringToCollection(value, set, sep);
                //include expression
                scanner.setIncludeExpressions(set);
            } else if (EXCLUDE_EXPRESSION.equalsIgnoreCase(name)) {
                CollectionUtil.stringToCollection(value, set, sep);
                //exclude expression
                scanner.setExcludeExpressions(set);
            } else {
                LOG.warn("Unknown property [{}] : [{}]", name, value);
            }
        }
        return scanner;
    }

    /**
     * 解析"<aop-config>"并添加aop属性。
     *
     * @param root 文档根节点。
     */
    private void parseAop(Element root) {
        List<Element> aops = getChildNodesByTagName(root, AOP_CONFIG);
        printSeparator(!aops.isEmpty());
        char[] sep = {',', ';'};
        for (Element aop : aops) {
            List<Element> aas = getChildNodesByTagName(aop, AOP_ACTION);
            for (Element e : aas) {
                String matches = e.getAttribute(MATCHES);
                String type = e.getAttribute(TYPE);
                String stacks = e.getAttribute(INTERCEPTOR_STACKS);
                String interceptors = e.getAttribute(INTERCEPTORS);
                LOG.info("Parse <aop-action> : [matches = {}, type = {}, interceptor-stacks = {}, interceptors = {}]",
                        matches, type, stacks, interceptors);
                AopAction aopAction = new AopAction();
                aopAction.setMatches(matches);
                aopAction.setType(Type.parseCode(type));
                if (StringUtil.isNotBlank(stacks)) {
                    List<String> list = new ArrayList<String>(4);
                    CollectionUtil.stringToCollection(stacks, list, sep);
                    aopAction.setInterceptorStacks(list);
                }
                if (StringUtil.isNotBlank(interceptors)) {
                    List<String> list = new ArrayList<String>(4);
                    CollectionUtil.stringToCollection(interceptors, list, sep);
                    aopAction.setInterceptors(list);
                }
                aopActions.add(aopAction);
            }
        }
    }

    /**
     * 获取指定父节点和节点名称的子节点集合。
     *
     * @param parent 指定的父节点。
     * @param name 指定的节点名称。
     *
     * @return 子节点集合。
     */
    private static List<Element> getChildNodesByTagName(Element parent, String name) {
        List<Element> eles = new ArrayList<Element>();
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            //System.out.println(child.getNodeName() + "," + child.getNodeType());
            if (Node.ELEMENT_NODE == child.getNodeType() && name.equals(child.getNodeName())) {
                eles.add((Element) child);
            }
        }
        return eles;
    }

    /**
     * TODO
     *
     * @param element
     * @param attribute
     *
     * @return
     */
    private String getTrimmedToNullString(Element element, String attribute) {
        String str = element.getAttribute(attribute);
        if (str != null)
            str = str.trim();
        if (str != null && str.length() == 0)
            str = null;
        return str;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 注入属性至指定的对象，并去除不支持的属性。
     *
     * @param obj 指定的对象。
     * @param properties 注入属性映射集合。
     * @param removeUnsupported 是否去除不支持的属性。
     *
     * @throws IntrospectionException 如果在内省期间发生异常。
     * @throws IllegalAccessException 如果底层方法不可访问。
     * @throws InvocationTargetException 如果底层方法抛出异常。
     */
    private static void injectProperties(Object obj, Map<String, Object> properties,
            boolean removeUnsupported) throws IntrospectionException, IllegalAccessException,
            InvocationTargetException {
        String cls = obj.getClass().getName();
        Map<String, PropertyDescriptor> supports = Injector.getSupportedProperties(obj.getClass());
        Iterator<Map.Entry<String, Object>> it = properties.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> e = it.next();
            String pName = e.getKey();
            PropertyDescriptor pd = supports.get(pName);
            if (pd == null) {
                LOG.error("Not supported property [{}] in [{}]", pName, cls);
                if (removeUnsupported)
                    it.remove();
            } else {
                Object value = e.getValue();
                //convert string to specific object
                if (value instanceof String) {
                    value = Injector.stringToObject((String) value, pd.getPropertyType());
                }
                pd.getWriteMethod().invoke(obj, value);
            }
        }
    }

    /**
     * 创建新对象。
     *
     * @param factory 指定的<code>ActionFactory</code>。
     * @param obj 传入的对象。
     *
     * @return 如果传入的对象为<code>String</code>或<code>Class</code>，返回由<code>ActionFactory</code>创建的实例对象；否则直接返回传入的对象。
     *
     * @throws ClassNotFoundException 如果没有找到具有指定名称的类。
     */
    private static Object newInstance(ActionFactory factory, Object obj) throws
            ClassNotFoundException {
        if (obj instanceof Class) {
            obj = factory.getObjectFactory().newInstance((Class<?>) obj);
        } else if (obj instanceof String) {
            obj = factory.getObjectFactory().newInstance(ClassUtil.loadClass((String) obj));
        }
        return obj;
    }

    /**
     * 由此Configuration对象中的配置属性创建一个新的ActionFactory对象。
     * 此Configuration对象中配置属性的变更不影响已生成的ActionFactory对象。
     *
     * @param <T> ActionFactory的类型。
     *
     * @return 生成的<code>ActionFactory</code>对象。
     *
     * @throws ConfigurationException 如果发生无效的配置。
     */
    public <T extends ActionFactory> T buildActionFactory() throws ConfigurationException {
        //create ActionFactory
        ActionFactory factory = createActionFactory(actionFactoryClass, actionFactoryProperties);
        printSeparator(!actionFactoryProperties.isEmpty());
        try {
            //扫描类集合
            Set<Class<?>> scanComponents = new LinkedHashSet<Class<?>>();
            //计算扫描类集合
            if (!classScanners.isEmpty()) {
                for (ClassScanner scanner : classScanners) {
                    LOG.info("Add classes scanner : " + scanner);
                    scanComponents.addAll(scanner.calculateScanComponents());
                }
                if (!scanComponents.isEmpty()) {
                    LOG.debug("Checking auto scan classes as follows :");
                    Iterator<Class<?>> it = scanComponents.iterator();
                    while (it.hasNext()) {
                        LOG.debug(it.next().toString());
                    }
                    LOG.debug("Finish check auto scan classes : " + scanComponents.size());
                } else {
                    LOG.debug("No auto scan classes");
                }
            }
            printSeparator(true);

            if (factory instanceof DefaultActionFactory) {
                DefaultActionFactory defaultFactory = (DefaultActionFactory) factory;

                //先加载指定配置的类，再加载自动搜索的类
                //排除指定配置的类
                Set<Class<?>> specified = new HashSet<Class<?>>();

                //interceptor
                for (Object obj : interceptors) {
                    obj = newInstance(factory, obj);
                    Map<String, Object> props = interceptorProperties.get(obj.getClass());
                    if (props != null) {
                        injectProperties(obj, props, true);
                    }
                    //add interceptor
                    defaultFactory.addInterceptors(obj);
                    specified.add(obj.getClass());
                }
                //auto-scan interceptors
                for (Class<?> cls : scanComponents) {
                    if (!specified.contains(cls))
                        defaultFactory.addInterceptors(cls);
                }
                //clear
                specified.clear();
                printSeparator(!defaultFactory.getInterceptors().isEmpty());

                //interceptor-stack
                for (Object obj : interceptorStacks) {
                    obj = newInstance(factory, obj);
                    Map<String, Object> props = interceptorStackProperties.get(obj.getClass());
                    if (props != null) {
                        injectProperties(obj, props, true);
                    }
                    //add interceptor stacks
                    defaultFactory.addInterceptorStacks(obj);
                    specified.add(obj.getClass());
                }
                //scan interceptorStacks
                for (Class<?> cls : scanComponents) {
                    if (!specified.contains(cls))
                        defaultFactory.addInterceptorStacks(cls);
                }
                //clear
                specified.clear();
                printSeparator(!defaultFactory.getInterceptorStacks().isEmpty());

                //result-type
                for (Object obj : resultTypes) {
                    obj = newInstance(factory, obj);
                    Map<String, Object> props = resultTypeProperties.get(obj.getClass());
                    if (props != null) {
                        injectProperties(obj, props, true);
                    }
                    //add result types
                    defaultFactory.addResultTypes(obj);
                    specified.add(obj.getClass());
                }
                //scan resultTypes
                for (Class<?> cls : scanComponents) {
                    if (!specified.contains(cls))
                        defaultFactory.addResultTypes(cls);
                }
                //clear
                specified.clear();
                printSeparator(!defaultFactory.getResultTypes().isEmpty());

                //result
                for (Object obj : results) {
                    obj = newInstance(factory, obj);
                    Map<String, Object> props = resultProperties.get(obj.getClass());
                    if (props != null) {
                        injectProperties(obj, props, true);
                    }
                    //add result types
                    defaultFactory.addResults(obj);
                    specified.add(obj.getClass());
                }
                //scan results
                for (Class<?> cls : scanComponents) {
                    if (!specified.contains(cls))
                        defaultFactory.addResults(cls);
                }
                //clear
                specified.clear();
                printSeparator(!defaultFactory.getResults().isEmpty());

                //action
                for (Object obj : actions) {
                    obj = newInstance(factory, obj);
                    Map<String, Object> props = actionProperties.get(obj.getClass());
                    if (props != null) {
                        injectProperties(obj, props, true);
                        //store the class properties for new instance of prototype action
                        Injector.putClassProperties(obj.getClass(), props);
                    }

                    //add result types
                    defaultFactory.addActions(obj);
                    specified.add(obj.getClass());
                }

                //scan actions
                for (Class<?> cls : scanComponents) {
                    if (!specified.contains(cls))
                        defaultFactory.addActions(cls);
                }
                //clear
                specified.clear();
                printSeparator(!defaultFactory.getActions().isEmpty());

                //specified path action
                for (Map.Entry<String, Map<String, Object>> e : pathProperties.entrySet()) {
                    String pathName = e.getKey();
                    Class<?> pathActionClass = pathActions.get(pathName);
                    Map<String, Object> allProps = new LinkedHashMap<String, Object>();
                    //class properties
                    allProps.putAll(actionProperties.get(pathActionClass));
                    //path properties
                    allProps.putAll(e.getValue());
                    //store the path properties for new instance of prototype action
                    Injector.putActionProperties(pathActionClass, pathName, allProps);
                }

                //actions' aop
                if (!aopActions.isEmpty()) {
                    LOG.info("Starting Aop Action");
                    AntPathMatcher matcher = new AntPathMatcher(defaultFactory.getPathSeparator() + "");
                    //已经匹配的路径
                    Set<String> existMatchPaths = new HashSet<String>();
                    //倒序，最后匹配的路径优先
                    for (int i = aopActions.size() - 1; i > -1; i--) {
                        in:
                        for (Map.Entry<String, DefaultActionProxy> e : defaultFactory.getActions().entrySet()) {
                            AopAction aa = aopActions.get(i);
                            String path = e.getKey();
                            if (matcher.match(aa.getMatches(), path)) {
                                if (existMatchPaths.contains(path))
                                    continue in;
                                existMatchPaths.add(path);
                                //exist can't be null by DefaultActionFactory
                                List<InterceptorProxy> exist = e.getValue().getInterceptorProxies();
                                List<InterceptorProxy> news = new ArrayList<InterceptorProxy>();
                                //TODO
                                if (CollectionUtil.isNotEmpty(aa.getInterceptorStacks())) {
                                    for (String stackName : aa.getInterceptorStacks()) {
                                        if (defaultFactory.getInterceptorStacks().containsKey(stackName)) {
                                            news.addAll(defaultFactory.getInterceptorStacks().get(stackName).getInterceptors());
                                        } else {
                                            LOG.warn("Can't find InterceptorStack [{}]", stackName);
                                        }
                                    }
                                }
                                if (CollectionUtil.isNotEmpty(aa.getInterceptors())) {
                                    for (String interceptorName : aa.getInterceptors()) {
                                        if (defaultFactory.getInterceptors().containsKey(interceptorName)) {
                                            news.add(defaultFactory.getInterceptors().get(interceptorName));
                                        } else {
                                            LOG.warn("Can't find Interceptor [{}]", interceptorName);
                                        }
                                    }
                                }
                                String existName = interceptorsToString(exist);
                                switch (aa.getType()) {
                                    case ADD_BEFORE: {
                                        exist.addAll(0, news);
                                        break;
                                    }
                                    case ADD_AFTER: {
                                        exist.addAll(news);
                                        break;
                                    }
                                    case OVERRIDE: {
                                        exist.clear();
                                        exist.addAll(news);
                                        break;
                                    }
                                }
                                LOG.info("Aop Action [{}] interceptors {} -> {}, matches {}",
                                        path, existName, interceptorsToString(exist), aa.toString());
                            }
                        }
                    }
                }
            }
            //give subclasses a chance to prepare factory
            afterActionFactoryBuild(factory);
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
        return (T) factory;
    }

    /**
     * 由指定的ActionFactory类型和属性集合创建ActionFactory的对象实例,可用于子类继承以覆写。
     * 如果存在，默认优先调用&lt;init&gt;(java.util.Map)的构造函数。
     *
     * @param <T> ActionFactory特定类型。
     * @param actionFactoryClass 指定的ActionFactory类型。
     * @param actionFactoryProperties ActionFactory的属性集合。
     *
     * @return ActionFactory的对象实例。
     *
     * @throws ConfigurationException 如果发生任何构造异常。
     */
    protected <T extends ActionFactory> T createActionFactory(
            Class<? extends ActionFactory> actionFactoryClass,
            Map<String, Object> actionFactoryProperties) throws ConfigurationException {
        ActionFactory factory = null;
        Constructor<? extends ActionFactory> con = null;
        try {
            //look up for (Map) constructor first
            con = actionFactoryClass.getDeclaredConstructor(Map.class);
        } catch (NoSuchMethodException ex) {
            LOG.info("No constructor {}, use {}.<init>(). ",
                    ex.getLocalizedMessage(), actionFactoryClass.getName());
        }

        try {
            factory = con == null
                    ? actionFactoryClass.newInstance()
                    : con.newInstance(actionFactoryProperties);
            //give subclasses a chance to prepare factory
            afterActionFactoryCreation(factory);
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
        return (T) factory;
    }

    /**
     * 用于子类继承, 在初始化ActionFactory前执行设置其一些特定的操作。
     * 默认情况下不做任何处理。
     *
     * @param factory 未初始化属性的{@link ActionFactory}。
     */
    protected void afterActionFactoryCreation(ActionFactory factory) {
    }

    /**
     * 用于子类继承, 在构造好ActionFactory后执行设置其一些特定的操作。
     * 默认情况下不做任何处理。
     *
     * @param factory 未初始化属性的{@link ActionFactory}。
     */
    protected void afterActionFactoryBuild(ActionFactory factory) {
    }

    /**
     * 返回ActionFactory。
     *
     * @param <T> Action工厂对象的类型。
     *
     * @return ActionFactory。
     *
     * @deprecated 由<code>{@link #buildActionFactory()}</code>取代。
     */
    @Deprecated
    public <T extends ActionFactory> T getFactory() {
        return buildActionFactory();
    }

    /**
     * 拦截器集合字符串显示名称。
     *
     * @param interceptors 拦截器集合。
     *
     * @return 显示名称。
     */
    private String interceptorsToString(List<InterceptorProxy> interceptors) {
        if (interceptors == null)
            return "null";
        int iMax = interceptors.size() - 1;
        if (iMax == -1)
            return "[]";
        StringBuilder msg = new StringBuilder();
        msg.append('[');
        for (int i = 0;; i++) {
            msg.append(interceptors.get(i).getName());
            if (i == iMax)
                return msg.append(']').toString();
            msg.append(", ");
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 设置ActionFactory的类型。
     *
     * @param actionFactoryClass 指定的ActionFactory类型。
     *
     * @return 此配置对象的引用。
     */
    public Configuration setActionFactoryClass(Class<? extends ActionFactory> actionFactoryClass) {
        this.actionFactoryClass = actionFactoryClass;
        return this;
    }

    /**
     * 添加ActionFactory的属性映射集合。
     *
     * @param actionFactoryProperties ActionFactory的属性映射集合。
     *
     * @return 此配置对象的引用。
     *
     * @see DefaultActionFactory
     */
    public Configuration addActionFactoryProperties(Map<String, Object> actionFactoryProperties) {
        this.actionFactoryProperties.putAll(actionFactoryProperties);
        return this;
    }

    /**
     * 添加Action集合。
     *
     * @param actions Action集合。
     *
     * @return 此配置对象的引用。
     */
    public Configuration addActions(Collection<Object> actions) {
        this.actions.addAll(actions);
        return this;
    }

    /**
     * 添加拦截栈集合。
     *
     * @param interceptorStacks 拦截栈集合。
     *
     * @return 此配置对象的引用。
     */
    public Configuration addInterceptorStacks(Collection<Object> interceptorStacks) {
        this.interceptorStacks.addAll(interceptorStacks);
        return this;
    }

    /**
     * 添加拦截器集合。
     *
     * @param interceptors 拦截器集合。
     *
     * @return 此配置对象的引用。
     */
    public Configuration addInterceptors(Collection<Object> interceptors) {
        this.interceptors.addAll(interceptors);
        return this;
    }

    /**
     * 添加结果类型集合。
     *
     * @param resultTypes 结果类型集合。
     *
     * @return 此配置对象的引用。
     */
    public Configuration addResultTypes(Collection<Object> resultTypes) {
        this.resultTypes.addAll(resultTypes);
        return this;
    }

    /**
     * 添加结果对象集合。
     *
     * @param results 结果对象集合。
     *
     * @return 此配置对象的引用。
     */
    public Configuration addResults(Collection<Object> results) {
        this.results.addAll(results);
        return this;
    }

    /**
     * 添加指定路径Action的属性集合。
     *
     * @param pathProperties 指定路径Action的属性集合。
     *
     * @return 此配置对象的引用。
     */
    public Configuration addPathProperties(Map<String, Map<String, Object>> pathProperties) {
        this.pathProperties.putAll(pathProperties);
        return this;
    }

    /**
     * 添加扫描配置。
     *
     * @param scanProperties 扫描配置
     *
     * @return 此配置对象的引用。
     *
     * @see ClassScanner
     * @see #parsecComponentClassScanner(java.util.Map)
     */
    public Configuration addComponentClassScanProperties(Map<String, String>... scanProperties) {
        for (Map<String, String> props : scanProperties) {
            classScanners.add(parsecComponentClassScanner(props));
        }
        return this;
    }

    /**
     * 添加Action Aop。
     *
     * @param aopActions Action Aop。
     *
     * @return 此配置对象的引用。
     */
    public Configuration addAopActions(Collection<? extends AopAction> aopActions) {
        this.aopActions.addAll(aopActions);
        return this;
    }
}
