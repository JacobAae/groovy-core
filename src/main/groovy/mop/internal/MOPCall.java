/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.mop.internal;

import java.util.LinkedList;

public class MOPCall {
    public LinkedList<DefaultMetaMethod> errorList;
    public String name;
    public Class baseClass;
    public Object receiver;
    public Object[] args;
    public Class[] types;

    public MOPCall(Class baseClass, Object receiver, String name, Object[] args) {
        this.name = name;
        this.baseClass = baseClass;
        this.receiver = receiver;
        this.args = args;
        this.types = MetaClassHelper.convertToTypeArray(args);
    }

}
