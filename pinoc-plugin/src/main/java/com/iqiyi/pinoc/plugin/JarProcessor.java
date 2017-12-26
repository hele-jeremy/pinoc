/**
 *
 * Copyright 2017 iQIYI.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.iqiyi.pinoc.plugin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;

/**
 * Created by Xiaofei on 2017/11/6.
 */

class JarProcessor {
    private static String getClassName(String name) {
        if (name.endsWith(".class")) {
            if (name.startsWith("com/iqiyi/pinoc/")) {
                return null;
            }
            if (name.startsWith("org/")) {
                return null;
            }
            if (name.startsWith("android/")) {
                return null;
            }
            if (!name.startsWith("com/iqiyi/ishow/") && !name.startsWith("com/iqiyi/qixiu/")) {
                return null;
            }
            int pos = name.indexOf(".class");
            int i;
            boolean found = false;
            for (i = pos - 1; i >= 0; --i) {
                if (name.charAt(i) == '/') {
                    found = true;
                    break;
                }
            }
            String className = found ? name.substring(i + 1, pos) : name.substring(0, pos);
            if (!className.startsWith("R$") && !"R".equals(className) && !"BuildConfig".equals(className)) {
                return className;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static void process(File source, File target) throws Exception {
        JarFile jarFile = new JarFile(source, false);
        Enumeration<JarEntry> entries = jarFile.entries();
        long s1 = 0L, s2 = 0L;
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(target));
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                jos.putNextEntry(new JarEntry(entry));
            } else {
                String className;
                if ((className = getClassName(entry.getName())) != null) {
                    JarEntry tmp = new JarEntry(entry.getName());
                    tmp.setComment(entry.getComment());
                    tmp.setExtra(entry.getExtra());
                    tmp.setMethod(ZipEntry.DEFLATED);
                    tmp.setTime(entry.getTime());
                    jos.putNextEntry(tmp);
                    InputStream is = jarFile.getInputStream(entry);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] bytes = new byte[1024];
                    int num;
                    int offset = 0;
                    while ((num = is.read(bytes, offset, 1024)) != -1) {
                        baos.write(bytes, 0, num);
                    }
                    is.close();
                    ClassReader classReader = new ClassReader(baos.toByteArray());
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
                    ClassVisitor cv = new PinocPluginClassVisitor(className, classWriter);
                    classReader.accept(cv, EXPAND_FRAMES);
                    byte[] code = classWriter.toByteArray();
                    jos.write(code, 0, code.length);
                } else {
                    JarEntry tmp = new JarEntry(entry.getName());
                    tmp.setComment(entry.getComment());
                    tmp.setExtra(entry.getExtra());
                    tmp.setMethod(entry.getMethod());
                    tmp.setTime(entry.getTime());
                    if (tmp.getMethod() == ZipEntry.STORED) {
                        tmp.setSize(entry.getSize());
                        tmp.setCompressedSize(entry.getCompressedSize());
                        tmp.setCrc(entry.getCrc());
                    }
                    jos.putNextEntry(tmp);
                    InputStream is = jarFile.getInputStream(entry);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] bytes = new byte[1024];
                    int num;
                    int offset = 0;
                    while ((num = is.read(bytes, offset, 1024)) != -1) {
                        baos.write(bytes, 0, num);
                    }
                    is.close();
                    jos.write(baos.toByteArray());
                }
            }
        }
        jos.close();
        jarFile.close();
    }
}
