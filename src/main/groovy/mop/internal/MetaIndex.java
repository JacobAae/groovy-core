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


import groovy.mop.MetaProperty;
import groovy.mop.internal.pcollection.PSet;

import java.util.*;

public class MetaIndex<T> {
    private static class Leaf<L> {
        private final PSet<L> pub,priv;
        public Leaf(PSet<L> pubIn, PSet<L> privIn) {
            this.pub = pubIn;
            this.priv = privIn;
        }
    }

    public static final MetaIndex EMPTY = new MetaIndex();
    private MetaIndex(){}

    public MetaIndex<T> getPublicAndPrivate() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public MetaIndex<T> getPublic() {
        // TODO Auto-generated method stub
        return null;
    }

    public MetaIndex<T> putLeaf(String name,
            PSet<DefaultMetaMethod> privateMethods,
            PSet<DefaultMetaMethod> publicMethods
    ) {
        // TODO Auto-generated method stub
        return null;
    }

    public PSet<T> get(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    
    
}
