/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.layoutlib.bridge.intensive.setup;

import com.android.SdkConstants;
import com.android.ide.common.rendering.api.ActionBarCallback;
import com.android.ide.common.rendering.api.AdapterBinding;
import com.android.ide.common.rendering.api.ILayoutPullParser;
import com.android.ide.common.rendering.api.IProjectCallback;
import com.android.ide.common.rendering.api.ResourceReference;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.resources.ResourceType;
import com.android.ide.common.resources.IntArrayWrapper;
import com.android.util.Pair;
import com.android.utils.ILogger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import com.google.android.collect.Maps;

@SuppressWarnings("deprecation") // For Pair
public class LayoutLibTestCallback extends ClassLoader implements IProjectCallback {

    private static final String PROJECT_CLASSES_LOCATION = "/testApp/MyApplication/build/intermediates/classes/debug/";
    private static final String PACKAGE_NAME = "com.android.layoutlib.test.myapplication";

    private final Map<Integer, Pair<ResourceType, String>> mProjectResources = Maps.newHashMap();
    private final Map<IntArrayWrapper, String> mStyleableValueToNameMap = Maps.newHashMap();
    private final Map<ResourceType, Map<String, Integer>> mResources = Maps.newHashMap();
    private final Map<String, Class<?>> mClasses = Maps.newHashMap();
    private final ILogger mLog;
    private final ActionBarCallback mActionBarCallback = new ActionBarCallback();

    public LayoutLibTestCallback(ILogger logger) {
        mLog = logger;
    }

    public void initResources() throws ClassNotFoundException {
        Class<?> rClass = loadClass(PACKAGE_NAME + ".R");
        Class<?>[] nestedClasses = rClass.getDeclaredClasses();
        for (Class<?> resClass : nestedClasses) {
            final ResourceType resType = ResourceType.getEnum(resClass.getSimpleName());

            if (resType != null) {
                final Map<String, Integer> resName2Id = Maps.newHashMap();
                mResources.put(resType, resName2Id);

                for (Field field : resClass.getDeclaredFields()) {
                    final int modifiers = field.getModifiers();
                    if (Modifier.isStatic(modifiers)) { // May not be final in library projects
                        final Class<?> type = field.getType();
                        try {
                            if (type.isArray() && type.getComponentType() == int.class) {
                                mStyleableValueToNameMap.put(
                                        new IntArrayWrapper((int[]) field.get(null)),
                                        field.getName());
                            } else if (type == int.class) {
                                final Integer value = (Integer) field.get(null);
                                mProjectResources.put(value, Pair.of(resType, field.getName()));
                                resName2Id.put(field.getName(), value);
                            } else {
                                mLog.error(null, "Unknown field type in R class: %1$s", type);
                            }
                        } catch (IllegalAccessException ignored) {
                            mLog.error(ignored, "Malformed R class: %1$s", PACKAGE_NAME + ".R");
                        }
                    }
                }
            }
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> aClass = mClasses.get(name);
        if (aClass != null) {
            return aClass;
        }
        String pathName = PROJECT_CLASSES_LOCATION.concat(name.replace('.', '/')).concat(".class");
        InputStream classInputStream = getClass().getResourceAsStream(pathName);
        if (classInputStream == null) {
            throw new ClassNotFoundException("Unable to find class " + name + " at " + pathName);
        }
        byte[] data;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            data = new byte[16384];
            while ((nRead = classInputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            data = buffer.toByteArray();
        } catch (IOException e) {
            // Wrap the exception with ClassNotFoundException so that caller can deal with it.
            throw new ClassNotFoundException("Unable to load class " + name, e);
        }
        aClass = defineClass(name, data, 0, data.length);
        mClasses.put(name, aClass);
        return aClass;
    }

    @Override
    public Object loadView(String name, Class[] constructorSignature, Object[] constructorArgs)
            throws Exception {
        Class<?> viewClass = findClass(name);
        Constructor<?> viewConstructor = viewClass.getConstructor(constructorSignature);
        viewConstructor.setAccessible(true);
        return viewConstructor.newInstance(constructorArgs);
    }

    @Override
    public String getNamespace() {
        return String.format(SdkConstants.NS_CUSTOM_RESOURCES_S,
                PACKAGE_NAME);
    }

    @Override
    public Pair<ResourceType, String> resolveResourceId(int id) {
        return mProjectResources.get(id);
    }

    @Override
    public String resolveResourceId(int[] id) {
        return mStyleableValueToNameMap.get(new IntArrayWrapper(id));
    }

    @Override
    public Integer getResourceId(ResourceType type, String name) {
        return mResources.get(type).get(name);
    }

    @Override
    public ILayoutPullParser getParser(String layoutName) {
        org.junit.Assert.fail("This method shouldn't be called by this version of LayoutLib.");
        return null;
    }

    @Override
    public ILayoutPullParser getParser(ResourceValue layoutResource) {
        return new LayoutPullParser(new File(layoutResource.getValue()));
    }

    @Override
    public Object getAdapterItemValue(ResourceReference adapterView, Object adapterCookie,
            ResourceReference itemRef, int fullPosition, int positionPerType,
            int fullParentPosition, int parentPositionPerType, ResourceReference viewRef,
            ViewAttribute viewAttribute, Object defaultValue) {
        return null;
    }

    @Override
    public AdapterBinding getAdapterBinding(ResourceReference adapterViewRef, Object adapterCookie,
            Object viewObject) {
        return null;
    }

    @Override
    public ActionBarCallback getActionBarCallback() {
        return mActionBarCallback;
    }
}
