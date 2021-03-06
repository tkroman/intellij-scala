package org.jetbrains.plugins.scala
package lang
package psi
package types

import com.intellij.openapi.progress.ProgressManager
import org.jetbrains.plugins.scala.extensions.ifReadAllowed
import org.jetbrains.plugins.scala.lang.psi.types.api.{TypeSystem, TypeVisitor, ValueType, Variance}
import org.jetbrains.plugins.scala.lang.psi.types.recursiveUpdate.{AfterUpdate, Update}
import org.jetbrains.plugins.scala.lang.refactoring.util.ScTypeUtil.AliasType
import org.jetbrains.plugins.scala.project.ProjectContextOwner

import scala.language.implicitConversions

trait ScType extends ProjectContextOwner {

  def typeSystem: TypeSystem = projectContext.typeSystem

  private var aliasType: Option[AliasType] = null

  final def isAliasType: Option[AliasType] = {
    if (aliasType == null) {
      ProgressManager.checkCanceled()
      aliasType = isAliasTypeInner
    }
    aliasType
  }

  private var unpacked: ScType = null

  final def unpackedType: ScType = {
    if (unpacked == null) {
      ProgressManager.checkCanceled()
      unpacked = unpackedTypeInner
    }
    unpacked
  }

  protected def isAliasTypeInner: Option[AliasType] = None

  override final def toString: String = ifReadAllowed(presentableText)(getClass.getSimpleName)

  def isValue: Boolean

  def isFinalType: Boolean = false

  def inferValueType: ValueType

  protected def unpackedTypeInner: ScType = ScExistentialType(this) match {
    case ScExistentialType(q, Seq())                                       => q
    case ScExistentialType(arg: ScExistentialArgument, Seq(w)) if w == arg => arg.upper
    case ex                                                                => ex
  }


  /**
   * This method is important for parameters expected type.
   * There shouldn't be any abstract type in this expected type.
   * todo rewrite with recursiveUpdate method
   */
  def removeAbstracts: ScType = this

  def equivInner(r: ScType, constraints: ConstraintSystem, falseUndef: Boolean): ConstraintsResult = {
    ConstraintsResult.Left
  }

  def updateSubtypes(updates: Array[Update], index: Int, visited: Set[ScType]): ScType = this

  def updateSubtypesVariance(update: (ScType, Variance) => AfterUpdate,
                             variance: Variance,
                             revertVariances: Boolean)
                            (implicit visited: Set[ScType]): ScType = this

  def visitType(visitor: TypeVisitor)

  def typeDepth: Int = 1

  def presentableText(implicit context: TypePresentationContext): String =
    typeSystem.presentableText(this, withPrefix = true)

  def canonicalText: String = typeSystem.canonicalText(this)
}

object ScType {
  implicit def recursiveExtensions(tp: ScType): recursiveUpdate.Extensions = new recursiveUpdate.Extensions(tp)
}

trait NamedType extends ScType {
  val name: String

  override def presentableText(implicit context: TypePresentationContext): String = name

  override def canonicalText: String = name
}
