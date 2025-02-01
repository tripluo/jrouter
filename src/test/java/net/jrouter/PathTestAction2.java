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
import net.jrouter.impl.PathTreeTest;
import org.springframework.stereotype.Component;

import static net.jrouter.impl.PathTreeTest.PATHS;

/**
 * PathTestAction2。
 *
 * @see PathTreeTest#PATHS
 */
@Component
@Namespace(name = "/%s")
public class PathTestAction2 {

    @Action(name = "xx/yy/zz")
    public String test0() {
        return PATHS[0];
    }

    @Action(value = "{k1}")
    public String test1() {
        return PATHS[1];
    }

    @Action("aa/b1/c1/d0")
    public String test2() {
        return PATHS[2];
    }

    @Action(name = "aa/b1/c1/d1")
    public String test3() {
        return PATHS[3];
    }

    @Action(value = "aa/b2/c2/d2")
    public String test4() {
        return PATHS[4];
    }

    @Action("aa/b3/c1/d1/*")
    public String test5() {
        return PATHS[5];
    }

    @Action("aa/b3/*/d1")
    public String test6() {
        return PATHS[6];
    }

    @Action("aa/b3/*/d1/{k2}")
    public String test7() {
        return PATHS[7];
    }

    @Action("aa/*/c1/d1")
    public String test8() {
        return PATHS[8];
    }

    @Action("aa/*/c1/d1/e1")
    public String test9() {
        return PATHS[9];
    }

    @Action("aa/b4/{k1}/d1")
    public String test10() {
        return PATHS[10];
    }

    @Action("aa/b4/{k1}/d2")
    public String test11() {
        return PATHS[11];
    }

    @Action("aa/b4/{k1}/d2/e1")
    public String test12() {
        return PATHS[12];
    }

    @Action("aa/b4/{k1}/d2/{k2}")
    public String test13() {
        return PATHS[13];
    }

    @Action("aa/b5/*/*/*/*")
    public String test14() {
        return PATHS[14];
    }

}
