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

package com.iqiyi.trojan.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by Xiaofei on 2017/8/19.
 */

public class TrojanPluginClassVisitor extends ClassVisitor {

    private String className;

    public TrojanPluginClassVisitor(String name, ClassWriter cw) {
        super(Opcodes.ASM5, cw);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        className = name;
        System.out.println("Name " + name + " signature " + signature + " superName " + superName);
    }

    private static boolean check(int access, int flag) {
        return (access & flag) != 0;
    }
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        System.out.println("Name " + name + " signature " + signature + " desc " + desc);
        if (name.equals("<init>") || name.equals("<clinit>")) {
            return mv;
        }
        if (check(access, Opcodes.ACC_ABSTRACT)
                || check(access, Opcodes.ACC_BRIDGE)
                || check(access, Opcodes.ACC_NATIVE)
                || check(access, Opcodes.ACC_SYNTHETIC)
                || check(access, Opcodes.ACC_STRICT)) {
            System.out.println("Case 0");
            return mv;
        }
        // generic type
        if (signature != null) {
            System.out.println("Case 1");
            return mv;
        }

            return new TrojanPluginMethodVisitor(api, mv, access, className, name, desc);

//        return new TestMethodVisitor(api, mv);
    }
}
