package com.github.mt2309.pony.CodeGen

import com.github.mt2309.pony.Common.{TypeId,TabbedBuilder}

import com.github.mt2309.pony.CompilationUnit.CompilationUnit

import com.github.mt2309.pony.Typer._

/**
 * User: mthorpe
 * Date: 29/05/2013
 * Time: 16:43
 */
final class CodeGenerator(val units: IndexedSeq[CompilationUnit], val output: String) {

  val modules: IndexedSeq[TypedModule] = units.map(_.typeIt).flatten

  def codeGen(): Unit = {
    val classes: IndexedSeq[(TypeId, TModuleMember, Int)] = (modules.map(_.classes).flatten ++ tPrimitiveTypes.map(t => t.typename -> t)).zipWithIndex.map(t => (t._1._1, t._1._2, t._2))

    val longArraySize = classes.size / 64 + 1

    val headerBuilder = new StringBuilder(CodeGenerator.headerString)
    val sourceBuilder = new StringBuilder(CodeGenerator.sourceString)

    sourceBuilder.append(s"\n\nvoid initialise(void)\n{\n  clazz_set_size = $longArraySize;\n")

    // Generate the header
    for (clazz <- classes) {
      headerBuilder.append(s"unsigned long * ${clazz._1}_id;\n")
      sourceBuilder.appendln(s"${clazz._1}_id = initialise_bit_set(${clazz._3});")(1)
    }

    headerBuilder.append("\n")
    sourceBuilder.append("}\n\n")

    for (clazz <- classes) {
      clazz._2 match {
        case conc: ConcreteClass => {
          headerBuilder.appendln(s"pony_clazz * ${clazz._1}_construct();")(0)
          sourceBuilder.append(conc.codegen(0))

          for (body <- conc.methods) {
            headerBuilder.append(s"variable** ${clazz._1}_${body._2.mangle}(pony_clazz*, variable**);\n")
            sourceBuilder.append(s"variable**\n${clazz._1}_${body._2.mangle}(pony_clazz* this, variable** args)\n")
            sourceBuilder.append(body._2.codegen(0))
          }
        }
        case _ =>
      }
    }

    headerBuilder.append("#endif")

    println(headerBuilder.mkString)
    println("\n\n\n\n\n")
    println(sourceBuilder.mkString)

  }

}

object CodeGenerator {

  val headerString = {
    "#include <stdlib.h>\n#include <stdio.h>\n#include <stdbool.h>\n\n" ++
      "#include \"pony_class.h\"\n\n#ifndef PONY_PROGRAM_H\n#define PONY_PROGRAM_H\n\nvoid initialise(void);\nunsigned int clazz_set_size;\n\n"
  }

  val sourceString = "#include \"pony_class_ids.h\"\n\n"
}
