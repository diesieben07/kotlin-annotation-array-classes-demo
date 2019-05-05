package de.takeweiland.kotlinannotationarrayclasses

import kotlinx.metadata.*
import kotlinx.metadata.jvm.JvmTypeExtensionVisitor
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class TestAnnotation(val cls: KClass<*>)

@TestAnnotation(Array<String>::class)
class TestClass {

    val plainArray: @TestAnnotation(Array<String>::class) String = ""
    val inArray: @TestAnnotation(Array<String>::class) String = ""
    val outArray: @TestAnnotation(Array<String>::class) String = ""

    val primitiveArrayClass: @TestAnnotation(IntArray::class) String = ""
    val plainClass: @TestAnnotation(TestClass::class) String = ""

}

fun main() {
    println(TestClass::class.findAnnotation<TestAnnotation>()?.cls)
    println(TestClass::class.findAnnotation<TestAnnotation>()?.cls?.java)

    // these produce a crash
//     println(TestClass::plainArray.returnType.findAnnotation<TestAnnotation>()?.cls)
//     println(TestClass::inArray.returnType.findAnnotation<TestAnnotation>()?.cls)
//     println(TestClass::outArray.returnType.findAnnotation<TestAnnotation>()?.cls)

    println(TestClass::primitiveArrayClass.returnType.findAnnotation<TestAnnotation>()?.cls)
    println(TestClass::plainClass.returnType.findAnnotation<TestAnnotation>()?.cls)

    println(TestClass::plainArray.returnType.annotations)
    println(TestClass::inArray.returnType.annotations)
    println(TestClass::outArray.returnType.annotations)

    val meta = TestClass::class.java.getAnnotation(Metadata::class.java)!!
    val header = with(meta) {
        KotlinClassHeader(kind, metadataVersion, bytecodeVersion, data1, data2, extraString, packageName, extraInt)
    }

    val classMeta = KotlinClassMetadata.read(header) as KotlinClassMetadata.Class
    classMeta.accept(object : KmClassVisitor() {
        override fun visitProperty(
            flags: Flags,
            name: String,
            getterFlags: Flags,
            setterFlags: Flags
        ): KmPropertyVisitor? {
            return object : KmPropertyVisitor() {
                override fun visitReturnType(flags: Flags): KmTypeVisitor? {
                    return object : KmTypeVisitor() {
                        override fun visitExtensions(type: KmExtensionType): KmTypeExtensionVisitor? {
                            return object : JvmTypeExtensionVisitor() {
                                override fun visitAnnotation(annotation: KmAnnotation) {
                                    println("$name has annotation $annotation")
                                }
                            }
                        }
                    }
                }
            }
        }
    })
}