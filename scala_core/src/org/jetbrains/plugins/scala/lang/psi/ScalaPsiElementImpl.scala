package org.jetbrains.plugins.scala.lang.psi

import _root_.com.intellij.extapi.psi.{StubBasedPsiElementBase, ASTWrapperPsiElement}
import com.intellij.psi.impl.CheckUtil
import com.intellij.psi.impl.source.SourceTreeToPsiMap
import com.intellij.psi.impl.source.tree.{ChangeUtil, CompositeElement}
import com.intellij.psi.stubs.{StubElement, IStubElementType}
import com.intellij.psi.tree.IElementType
import com.intellij.lang.ASTNode
import com.intellij.psi.{PsiElement, StubBasedPsiElement}
import stubs.elements.wrappers.DummyASTNode

/**         
  @author ven
*/
abstract class ScalaPsiElementImpl(node: ASTNode) extends ASTWrapperPsiElement(node) with ScalaPsiElement {

  private val _locked = new ThreadLocal[Int] {
    override def initialValue: Int = 0
  }

  override protected def locked = _locked.get > 15

  override protected def lock(handler: => Unit) {
    if (!locked) {
      _locked.set(_locked.get + 1)
      handler
    }
  }

  override protected def unlock = _locked.set(0)

  // todo override in more specific cases
  override def replace(newElement: PsiElement): PsiElement = {
    getParent.getNode.replaceChild(getNode, newElement.getNode)
    newElement
  }
}

abstract class ScalaStubBasedElementImpl[T <: PsiElement]
extends StubBasedPsiElementBase[StubElement[T]](DummyASTNode) with ScalaPsiElement with StubBasedPsiElement[StubElement[T]] {
  override def getElementType(): IStubElementType[StubElement[T], T] = {
    if (getNode != DummyASTNode && getNode != null) getNode.getElementType.asInstanceOf[IStubElementType[StubElement[T], T]]
    else getStub.getStubType.asInstanceOf[IStubElementType[StubElement[T], T]]
  }
}