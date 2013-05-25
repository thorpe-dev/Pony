package com.github.mt2309.pony.Typer

import com.github.mt2309.pony.Common._
import com.github.mt2309.pony.AST._
import com.github.mt2309.pony.CompilationUnit.{QualifiedCompilationUnits, UnqualifiedCompilationUnits}
import com.github.mt2309.pony.Loader.Loader
import com.github.mt2309.pony.AST.Actor
import com.github.mt2309.pony.AST.Module
import com.github.mt2309.pony.AST.Trait
import com.github.mt2309.pony.AST.Object

/**
 * User: mthorpe
 * Date: 21/05/2013
 * Time: 15:57
 */
class TopTypeModule(val filename: Filename, moduleScope: TypeScope, module: Module) {

  val imports: UnqualifiedCompilationUnits = {
    val unqualifiedIm = for (i <- module.imports.filter(_.toType.isEmpty)) yield Loader.load(filename, i.importName)
    new UnqualifiedCompilationUnits(unqualifiedIm)
  }

  val typedImports: QualifiedCompilationUnits = {
    val qualified = (for (i <- module.imports.filter(_.toType.isDefined)) yield i.toType.get -> Loader.load(filename, i.importName)).toMap
    new QualifiedCompilationUnits(qualified)
  }

  val scope: TypeScope = moduleScope ++ imports.units.map(_.typeScope).flatten

  def typeCheck: PreTypedModule = {
    val imp = typedImports -> imports
    new PreTypedModule(imp, module.classes)(new Scope(scope, imp, Map.empty, filename))
  }

}
