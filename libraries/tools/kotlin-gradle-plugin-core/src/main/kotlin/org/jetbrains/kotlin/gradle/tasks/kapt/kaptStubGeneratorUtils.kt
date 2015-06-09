package org.jetbrains.kotlin.gradle.tasks.kapt

import org.jetbrains.org.objectweb.asm.*
import org.jetbrains.org.objectweb.asm.Opcodes.*
import java.io.File

/*
    This file should be a part of AnnotationProcessingManager in kotlin-gradle-plugin,
    but org.jetbrains.org.objectweb.asm can't be used there.
 */

private fun generateKotlinAptAnnotation(outputDirectory: File): File {
    val packageName = "__gen"
    val className = "KotlinAptAnnotation"
    val classFqName = "$packageName/$className"

    val bytes = with (ClassWriter(0)) {
        visit(49, ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE + ACC_ANNOTATION, classFqName,
                null, null, arrayOf("java/lang/annotation/Annotation"))
        visitSource(null, null)
        visitEnd()
        toByteArray()
    }

    val injectPackage = File(outputDirectory, packageName)
    injectPackage.mkdirs()
    val outputFile = File(injectPackage, "$className.class")
    outputFile.writeBytes(bytes)

    return outputFile
}

private fun generateAnnotationProcessorWrapper(
        processorFqName: String,
        packageName: String,
        outputDirectory: File,
        className: String,
        taskQualifier: String
): File {
    val classFqName = "$packageName/$className"

    val bytes = with (ClassWriter(0)) {
        val superClass = "org/jetbrains/kotlin/annotation/AnnotationProcessorWrapper"

        visit(49, ACC_PUBLIC + ACC_SUPER, classFqName, null,
                superClass, null)

        visitSource(null, null)

        with (visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)) {
            visitVarInsn(ALOAD, 0)
            visitLdcInsn(processorFqName)
            visitLdcInsn(taskQualifier)
            visitMethodInsn(INVOKESPECIAL, superClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", false)
            visitInsn(RETURN)
            visitMaxs(3 /*max stack*/, 1 /*max locals*/)
            visitEnd()
        }

        visitEnd()
        toByteArray()
    }
    val outputFile = File(outputDirectory, "$className.class")
    outputFile.writeBytes(bytes)

    return outputFile
}