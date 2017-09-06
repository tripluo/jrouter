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
package jrouter.bytecode.javassist;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javassist.ClassPool;
import javassist.CtMethod;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Mnemonic;
import jrouter.NotFoundException;
import jrouter.util.AntPathMatcher;
import jrouter.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static javassist.bytecode.Opcode.*;
import jrouter.util.MethodUtil;

/**
 * 提供基于javassist的根据{@link Method}底层方法解析匹配指定字符串的方法检查器。
 */
public class JavassistMethodChecker {

    /** LOG */
    private static final Logger LOG = LoggerFactory.getLogger(JavassistMethodChecker.class);

    private static final String OPCODES[] = Mnemonic.OPCODE;

    /** 方法名匹配器 */
    private final AntPathMatcher methodMatcher = new AntPathMatcher(".");

    /** 方法参数匹配器 */
    private final AntPathMatcher parameterMatcher = new AntPathMatcher(",");

    /** source pattern */
    private final String sourcePattern;

    /** match all */
    private final List<MethodInfo> allMatch;

    /** at least match one */
    private final List<MethodInfo> anyMatch;

    /**
     * 根据指定的匹配模式构造JavassistMethodChecker。
     *
     * @param pattern 匹配字符串，包含 & 或 | 运算符。
     */
    public JavassistMethodChecker(String pattern) {
        this.allMatch = new ArrayList<MethodInfo>();
        this.anyMatch = new ArrayList<MethodInfo>();
        this.sourcePattern = pattern;
        this.prasePattern(pattern);
    }

    /**
     * 分析指定的{@code Method}对象体，检查是否匹配指定的模式。
     *
     * @param method 待检查的Method对象。
     *
     * @return 是否匹配指定的模式。
     */
    public boolean check(Method method) {
        List<MethodInfo> bodyMethods = new ArrayList<MethodInfo>();
        try {
            CtMethod ctMethod = toMethod(method);
            javassist.bytecode.MethodInfo info = ctMethod.getMethodInfo2();
            ConstPool pool = info.getConstPool();
            CodeAttribute code = info.getCodeAttribute();
            if (code == null)
                return false;
            CodeIterator iter = code.iterator();
            while (iter.hasNext()) {
                int pos = iter.next();
                int opcode = iter.byteAt(pos);
                if (opcode > OPCODES.length || opcode < 0)
                    throw new BadBytecode("Invalid opcode, opcode: " + opcode + " pos: " + pos);

                switch (opcode) {
                    case INVOKEVIRTUAL:
                    case INVOKESPECIAL:
                    case INVOKESTATIC:
                        bodyMethods.add(methodInfo(pool, iter.u16bitAt(pos + 1)));
                        break;
                    case INVOKEINTERFACE:
                        bodyMethods.add(interfaceMethodInfo(pool, iter.u16bitAt(pos + 1)));
                        break;
                    default:
                        ;
                }
            }
        } catch (javassist.NotFoundException ex) {
            LOG.warn("Check method error : " + ex.getMessage());
        } catch (BadBytecode ex) {
            LOG.warn("Check method error : " + ex.getMessage());
        }

        BitSet matchedAll = new BitSet(allMatch.size());
        //pass true if any list size < 2
        boolean matchedAny = (anyMatch.size() < 2);
        //match
        for (MethodInfo bodyMethod : bodyMethods) {
            for (int i = 0; i < allMatch.size(); i++) {
                if (!matchedAll.get(i) && matchMethod(bodyMethod, allMatch.get(i))) {
                    matchedAll.set(i);
                }
            }
            if (!matchedAny) {
                for (MethodInfo any : anyMatch) {
                    matchedAny = matchMethod(bodyMethod, any);
                    if (matchedAny)
                        break;
                }
            }
        }
        //match all
        if (matchedAll.cardinality() != allMatch.size()) {
            LOG.warn("Not match all for pattern [{}] in method [{}] ", sourcePattern, MethodUtil.getMethod(method));
            return false;
        }
        //查无匹配
        if (!matchedAny) {
            LOG.warn("Not match at least one for pattern [{}] in methed [{}]", sourcePattern, MethodUtil.getMethod(method));
            return false;
        }
        return true;
    }

    //build MethodInfo from method's const pool
    private static MethodInfo methodInfo(ConstPool pool, int index) {
        MethodInfo mi = new MethodInfo();
        mi.setMethodName(pool.getMethodrefClassName(index) + "." + pool.getMethodrefName(index));
        mi.setParametersDescription(MethodInfo.toParametersDescription(toClassNames(pool.getMethodrefType(index))));
        return mi;
    }

    //build MethodInfo from method's const pool
    private static MethodInfo interfaceMethodInfo(ConstPool pool, int index) {
        MethodInfo mi = new MethodInfo();
        mi.setMethodName(pool.getInterfaceMethodrefClassName(index) + "." + pool.getInterfaceMethodrefName(index));
        mi.setParametersDescription(MethodInfo.toParametersDescription(toClassNames(pool.getInterfaceMethodrefType(index))));
        return mi;
    }

    /**
     * 判断方法是否匹配指定的模式（方法名称/参数）。
     */
    private boolean matchMethod(MethodInfo method, MethodInfo pattern) {
        if (method == null)
            return false;
        if (pattern == null || pattern.methodName == null)
            return true;
        return this.methodMatcher.match(pattern.methodName, method.methodName)
                && (pattern.parametersDescription == null
                        ? true
                        : this.parameterMatcher.match(pattern.parametersDescription, method.parametersDescription));
    }

    /** Method object */
    private static final class MethodInfo {

        //simplify name
        private static final String JAVA_LANG = "java.lang.";

        //method name
        private String methodName;

        //method parameters
        private String parametersDescription;

        //parse jrouter.ActionInvocation.invoke(**) to name/parameters.
        static MethodInfo toMethodInfo(String des) {
            MethodInfo mi = null;
            if (StringUtil.isNotBlank(des)) {
                mi = new MethodInfo();
                int n = des.indexOf('(');
                int m = des.lastIndexOf(')');
                if (n > -1 && n < m) {
                    mi.setMethodName(des.substring(0, n));
                    mi.setParametersDescription(des.substring(n + 1, m));
                } else {
                    mi.setMethodName(des);
                }
            }
            return mi;
        }

        //parse parameters to String
        static String toParametersDescription(Collection<String> params) {
            if (params == null || params.isEmpty())
                return "";
            StringBuilder sb = new StringBuilder();
            Iterator<String> i = params.iterator();
            for (;;) {
                String e = i.next();
                sb.append(e);
                if (!i.hasNext())
                    return sb.toString();
                sb.append(",");
            }
        }

        //set method name
        public void setMethodName(String methodName) {
            if (methodName.startsWith(JAVA_LANG))
                methodName = methodName.substring(JAVA_LANG.length());
            this.methodName = methodName;
        }

        //set parameters description, ',' as separator
        public void setParametersDescription(String parametersDescription) {
            if (parametersDescription.startsWith(JAVA_LANG))
                parametersDescription = parametersDescription.substring(JAVA_LANG.length());
            //remove "java.lang."
            parametersDescription = parametersDescription.replaceAll("\\s*,\\s*(?:java.lang.)?", ",");
            this.parametersDescription = parametersDescription;
        }

        @Override
        public String toString() {
            return "MethodInfo{" + methodName + "(" + (parametersDescription == null ? "" : parametersDescription) + ")}";
        }

    }

    //parse &| as separator, no group
    private void prasePattern(String pattern) {
        pattern = StringUtil.trim(pattern, '&', '|');
        List<String> all = new ArrayList<String>(4);
        List<String> any = new ArrayList<String>(4);
        //default & as first if no last
        char before = Character.MIN_VALUE;
        int p = 0;
        int i = 0;
        String name = null;
        for (; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            switch (c) {
                case '&':
                case '|':
                    name = StringUtil.trim(pattern.substring(p, i));
                    if (StringUtil.isNotEmpty(name)) {
                        //handle first
                        if (before == Character.MIN_VALUE) {
                            before = c;
                        }
                        if (before == '&') {
                            all.add(name);
                        } else if (before == '|') {
                            any.add(name);
                        }
                    }
                    p = i + 1;
                    before = c;
                    break;
                default:
                    break;
            }
        }
        if (p < i) {
            name = StringUtil.trim(pattern.substring(p, i));
            if (StringUtil.isNotEmpty(name)) {
                //if no separator
                if (before == Character.MIN_VALUE || before == '&') {
                    all.add(name);
                } else if (before == '|') {
                    any.add(name);
                }
            }
        }
        for (String s : all) {
            allMatch.add(MethodInfo.toMethodInfo(s));
        }
        for (String s : any) {
            anyMatch.add(MethodInfo.toMethodInfo(s));
        }
    }

    /**
     * Convert Method to CtMethod.
     *
     * @param m Method.
     *
     * @return CtMethod.
     *
     * @throws NotFoundException
     */
    private static CtMethod toMethod(Method m) throws javassist.NotFoundException {
        ClassPool classPool = ClassPool.getDefault();
        Class[] params = m.getParameterTypes();
        String[] paramTypeNams = new String[params.length];
        for (int i = 0; i < paramTypeNams.length; i++) {
            paramTypeNams[i] = params[i].getName();
        }
        return classPool.get(m.getDeclaringClass().getName()).
                getDeclaredMethod(m.getName(), classPool.get(paramTypeNams));
    }

    /**
     * Convert JVM method parameters descriptor to java class names.
     *
     * @see javassist.bytecode.InstructionPrinter#print(javassist.CtMethod)
     * @see javassist.bytecode.Descriptor#toClassName(java.lang.String)
     */
    private static List<String> toClassNames(String descriptor) {
        List<String> params = new ArrayList<String>(3);
        out:
        for (int i = 0; i < descriptor.length(); i++) {
            char c = descriptor.charAt(i);
            int arrayDim = 0;
            while (c == '[') {
                ++arrayDim;
                c = descriptor.charAt(++i);
            }
            String name = "";
            switch (c) {
                case 'L': {
                    int i2 = descriptor.indexOf(';', i++);
                    name = descriptor.substring(i, i2).replace('/', '.');
                    i = i2;
                    break;
                }
                case 'V': {
                    name = "void";
                    break;
                }
                case 'I': {
                    name = "int";
                    break;
                }
                case 'B': {
                    name = "byte";
                    break;
                }
                case 'J': {
                    name = "long";
                    break;
                }
                case 'D': {
                    name = "double";
                    break;
                }
                case 'F': {
                    name = "float";
                    break;
                }
                case 'C': {
                    name = "char";
                    break;
                }
                case 'S': {
                    name = "short";
                    break;
                }
                case 'Z': {
                    name = "boolean";
                    break;
                }
                //start
                case '(': {
                    break;
                }
                //end
                case ')': {
                    break out;
                }
                default: {
                    //ignore
                    //throw new RuntimeException("bad descriptor: " + descriptor);
                }
            }
            if (!name.isEmpty()) {
                if (arrayDim == 0) {
                    params.add(name);
                } else {
                    StringBuilder namyArray = new StringBuilder(name);
                    do {
                        namyArray.append("[]");
                    } while (--arrayDim > 0);
                    params.add(namyArray.toString());
                }
            }
        }
        return params;
    }
}
