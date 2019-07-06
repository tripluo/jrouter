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
 * 测试路径 URLTestAction。
 */
@Namespace(name = "/")
public class URLTestAction {

    @Action
    public String test100() {
        return "/test100";
    }

    @Action(name = "")
    public String test101() {
        return "/test101";
    }

    @Action(name = " ")
    public String test102() {
        return "/test102";
    }

    @Action(name = "    ")
    public String test103() {
        return "/test103";
    }

    @Action(name = "/")
    public String test104() {
        return "/";
    }

    @Action(name = "a")
    public String test200() {
        return "/a";
    }

    @Action(name = " b")
    public String test201() {
        return "/b";
    }

    @Action(name = " c ")
    public String test202() {
        return "/c";
    }

    @Action(name = "/d/")
    public String test203() {
        return "/d";
    }

    @Action(name = " //e ////")
    public String test204() {
        return "/e";
    }

    @Action(name = "test")
    public String test300() {
        return "/test";
    }

    @Action(name = "/test1")
    public String test301() {
        return "/test1";
    }

    @Action(name = "test2/")
    public String test302() {
        return "/test2";
    }

    @Action(name = "/test3/")
    public String test303() {
        return "/test3";
    }

    @Action(name = "///test4 //")
    public String test304() {
        return "/test4";
    }

    @Action(name = "test/abc")
    public String test400() {
        return "/test/abc";
    }

    @Action(name = "/test1/abc")
    public String test401() {
        return "/test1/abc";
    }

    @Action(name = "/test2//abc/")
    public String test402() {
        return "/test2/abc";
    }

    @Action(name = "///test3/abc////")
    public String test403() {
        return "/test3/abc";
    }

    @Action(name = "   ///test4/abc   /")
    public String test404() {
        return "/test4/abc";
    }
}
