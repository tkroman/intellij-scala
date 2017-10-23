package org.jetbrains.plugins.scala
package lang
package psi
package types
package result

import com.intellij.psi.PsiElement
import org.jetbrains.plugins.scala.project.ProjectContext

/**
  * @author ilyas
  */
sealed abstract class TypeResult[+T <: ScType](implicit val projectContext: ProjectContext) {

  def map[U <: ScType](f: T => U): TypeResult[U]

  def flatMap[U <: ScType](f: T => TypeResult[U]): TypeResult[U]

  def withFilter(f: T => Boolean): TypeResult[T]

  def foreach[B](f: T => B)

  def get: T

  def isEmpty: Boolean

  final def isDefined: Boolean = !isEmpty

  final def exists(f: T => Boolean): Boolean = withFilter(f).isDefined

  final def getOrElse[U >: T](default: => U): U = if (isEmpty) default else get

  final def toOption: Option[T] = if (isEmpty) None else Some(get)
}

object TypeResult {
  def fromOption(o: Option[ScType])
                (implicit pc: ProjectContext): TypeResult[ScType] = o match {
    case Some(t) => Success(t)
    case None => Failure("")
  }
}

class Success[T <: ScType] private(private val result: T)
                                  (implicit context: ProjectContext) extends TypeResult[T] {

  def map[U <: ScType](f: T => U): Success[U] = Success(f(result))

  def flatMap[U <: ScType](f: T => TypeResult[U]): TypeResult[U] = f(result)

  def withFilter(f: T => Boolean): TypeResult[T] = if (f(result)) this else Failure("Wrong type")

  def foreach[B](f: T => B): Unit = f(result)

  def get: T = result

  def isEmpty: Boolean = false

  override def equals(other: Any): Boolean = other match {
    case Success((otherResult, _)) => result == otherResult
    case _ => false
  }

  override def hashCode(): Int = result.hashCode()

  override def toString = s"Success($result)"
}

object Success {

  def apply[T <: ScType](result: T)
                        (implicit context: ProjectContext): Success[T] = new Success(result)

  def unapply[T <: ScType](success: Success[T]): Option[(T, Option[PsiElement])] =
    Option(success)
      .map(_.result)
      .map((_, None))
}

case class Failure(private val cause: String)
                  (implicit context: ProjectContext) extends TypeResult[Nothing] {

  def map[U <: ScType](f: Nothing => U): this.type = this

  def flatMap[U <: ScType](f: Nothing => TypeResult[U]): this.type = this

  def withFilter(f: Nothing => Boolean): this.type = this

  def foreach[B](f: Nothing => B): Unit = {}

  def get: Nothing = throw new NoSuchElementException("Failure.get")

  def isEmpty: Boolean = true
}