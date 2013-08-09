/*
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

import groovy.mop.*;
import groovy.mop.internal.pcollection.PSet;

import static groovy.mop.internal.MetaClassHelper.*;

/**
 * This is the implementation of a meta class for Groovy according to
 * MetaObjectProtocol. This class is implemented in the style of an 
 * persistent collection.
 *   
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 * @see MetaClass
 */
public class DefaultMetaClass {

    // -------------  immutable values ----------
    private final Class<?> theClass;
    private final DefaultRealm realm;

    private MetaClassRef[] dependencies;
    private NameVisibilityIndex<MetaProperty> properties = null;
    private NameVisibilityIndex<DefaultMetaMethod> methods = null;

    public DefaultMetaClass(DefaultRealm realm, Class<?> theClass) {
        this.theClass = theClass;
        this.realm = realm;
    }

    // -------------------------------------------------------------------
    //          Methods for interfacing with MetaClassHandle
    // -------------------------------------------------------------------

    public PSet<MetaProperty> getProperties(Class view, String name) {
        computeProperties();
        return properties.getPubPriv(name);
    }

    public PSet<DefaultMetaMethod> getMethods(Class view, String name) {
        NameVisibilityIndex<DefaultMetaMethod> index = getMethodIndex();
        PSet<DefaultMetaMethod> methods = index.getPubPriv(name);
        for (MetaClassRef handle : getDependencies()) {
            methods = methods.append(handle.getRef().getMethods(view, name));
        }
        return methods;
    }
    public Class<?> getTheClass() {
        return theClass;
    }

    // -------------------------------------------------------------------

    private MetaClassRef[] getDependencies() {
        if (dependencies!=null) return dependencies;
        LinkedList<MetaClassRef> l = new LinkedList();
        //TODO: maybe save information that this is an interface in meta class itself, instead of always asking here
        if (theClass!=Object.class && !theClass.isInterface()) {
            MetaClassRef ref = realm.getMetaClassHandleReferece(theClass.getSuperclass());
            l.add(ref);
        }
        //TODO: add outer classes too!
        for (Class<?> ci : theClass.getInterfaces()) {
            MetaClassRef ref = realm.getMetaClassHandleReferece(ci);
            l.add(ref);
        }
        //TODO: use constant for array
        MetaClassRef[] res = l.toArray(new MetaClassRef[0]);
        dependencies = res;
        return res;
    }

    public void computeProperties() {
        if (properties!=null) return;
        //properties = PropertyHelper.createIndex(theClass, realm);
    }

    public NameVisibilityIndex<DefaultMetaMethod> getMethodIndex() {
        if (methods!=null) return methods;
        methods = MethodHelper.createIndex(this);
        return methods;
    }

    public DefaultRealm getRealm() {
        return realm;
    }

    public NameVisibilityIndex<DefaultMetaMethod> getExtensions() {
        // TODO Auto-generated method stub
        return NameVisibilityIndex.EMPTY;
    }

    public void selectMethod(MOPCall call) {
        PSet<DefaultMetaMethod> methods = getMethods(call.baseClass, call.name);
        setCallTargetWithDistanceCalculator(methods,call);
    }

    private void setCallTargetWithDistanceCalculator(PSet<DefaultMetaMethod> methods, MOPCall call) {
        long savedDistance = Long.MAX_VALUE;
        DefaultMetaMethod ret = null;
        Class[] types = call.types;
        LinkedList<DefaultMetaMethod> errorList = null;
        for (DefaultMetaMethod mm : methods) {
            long distance = calculateParameterDistance(types, mm.getParameterClasses()); 
            if (distance==-1 || distance>savedDistance) continue;
            if (distance==0) {
                transformHandleForTypes(call, mm);
                return;
            }
            if (distance<savedDistance) {
                errorList = null;
                savedDistance = distance;
                ret = mm;
                continue;
            } 
            //distance==savedDistance
            if (errorList==null) {
                errorList = new LinkedList<>();
                errorList.add(ret);
            }
            errorList.add(mm);
        }
        if (errorList!=null) {
            call.errorList = errorList;
        } else if (ret!=null) {
            call.target = ret.getTarget();
        }
    }

    private void transformHandleForTypes(MOPCall call, DefaultMetaMethod ret) {
        call.target = ret.getTarget();
    }

    /*

    @Override
    public List<? extends MetaMethod> getMetaMethods(String name, Class... argumentTypes) {
        List<DefaultMetaMethod> list = getMetaMethods(name);
        SerialRemoveList<DefaultMetaMethod> res = new SerialRemoveList<>(list);
        res = res.minus(MethodHandles.insertArguments(METAMETHOD_ISASSIGNABLE,0, (Object[]) argumentTypes));
        return res;
    }

    @Override
    public List<? extends MetaMethod> respondsTo(String name, Object... arguments) {
        Class[] types = convertToTypeArray(arguments);
        return getMetaMethods(name, types);
    }


    private static MethodHandle unreflect(Method m) {
        try {
            return LOOKUP.unreflect(m);
        } catch (IllegalAccessException e) {
            ExceptionUtils.sneakyThrow(e);
        }
        return null;
    }

    private static List<DefaultMetaMethod> mergeMethods(List<DefaultMetaMethod> listWithOverrides, List<DefaultMetaMethod> origin) {
        // name is same
        if (listWithOverrides.size()==0) return origin;
        if (origin.size()==0) return listWithOverrides;
        List<DefaultMetaMethod> skipList = new ArrayList(origin);
        for (ListIterator<DefaultMetaMethod> iter = skipList.listIterator(); iter.hasNext(); ) {
            DefaultMetaMethod oldMM = iter.next();
            for (DefaultMetaMethod newMM : listWithOverrides) {
                if (oldMM.getTarget().type().equals(newMM.getTarget().type())) iter.remove();
            }
        }
        //TODO: use persistent list here
        skipList.addAll(listWithOverrides);
        return skipList;
    }

    private MetaIndex<DefaultMetaMethod> getParentPublicMethods() {
        if (parents.length==0) return MetaIndex.EMPTY;
        return parents[0].getMetaMethodIndex().getPublic();
    }

    private List<DefaultMetaMethod> getParentPublicMethods(String name) {
        return getParentPublicMethods().getList(name);
    }

    private void setCallTarget(MOPCall call, String name, Class... types) {
        List<DefaultMetaMethod> methods = getMetaMethods(name);
        if (methods.size() == 1) {
            DefaultMetaMethod mm = methods.get(0);
            if (!SignatureHelper.canBeCalledWithTypes(mm, types)) return;
            transformHandleForTypes(call, mm, types); 
        } else {
            getCallTargetWithDistanceCalculator(call, methods, name, types);
        }
    }

*/

    @Override
    public String toString() {
        return "MetaClass(realm:"+getRealm()+",class:"+theClass.getName()+")";
    }

}

