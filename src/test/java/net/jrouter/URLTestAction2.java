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
package net.jrouter;

import net.jrouter.annotation.Action;
import net.jrouter.annotation.Namespace;

/**
 * 测试路径 URLTestAction2。
 */
@Namespace(name = "/url")
public class URLTestAction2 extends URLTestAction {

    @Action
    public String test100() {
        return "/url/test100";
    }

    @Action()
    public String test101() {
        return "/url/test101";
    }

    @Action("a2")
    public String test200() {
        return "/url/a2";
    }

    @Action(" b2")
    public String test201() {
        return "/url/b2";
    }

    @Action(" c2 ")
    public String test202() {
        return "/url/c2";
    }

    @Action("/url_d2/")
    public String test203() {
        return "/url_d2";
    }

    @Action(name = " //url_e2 ////")
    public String test204() {
        return "/url_e2";
    }

    @Action(name = "test")
    public String test300() {
        return "/url/test";
    }

    @Action(name = "/url_test1")
    public String test301() {
        return "/url_test1";
    }

    @Action(name = "test2/")
    public String test302() {
        return "/url/test2";
    }

    @Action(name = "/url_test3/")
    public String test303() {
        return "/url_test3";
    }

    @Action(name = "///url_test4 //")
    public String test304() {
        return "/url_test4";
    }

    @Action(name = "test/abc")
    public String test400() {
        return "/url/test/abc";
    }

    @Action(name = "/url_test1/abc")
    public String test401() {
        return "/url_test1/abc";
    }

    @Action(name = "/url_test2/abc/")
    public String test402() {
        return "/url_test2/abc";
    }

    @Action(name = "///url_test3/abc////")
    public String test403() {
        return "/url_test3/abc";
    }

    @Action(name = "   ///url_test4/abc   /")
    public String test404() {
        return "/url_test4/abc";
    }
}
