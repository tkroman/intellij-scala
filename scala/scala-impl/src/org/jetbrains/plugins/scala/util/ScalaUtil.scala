package org.jetbrains.plugins.scala.util

import java.io.File

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.plugins.scala.project.{ModuleExt, ProjectExt}

/**
  * User: Alexander Podkhalyuzin
  * Date: 16.11.11
  */

object ScalaUtil {

  def getScalaPluginSystemPath: String =
    PathManager.getSystemPath + "/scala"

  def createTmpDir(prefix: String): File = {
    val tmpDir = File.createTempFile(prefix, "")
    tmpDir.delete()
    tmpDir.mkdir()
    tmpDir
  }

  def findVirtualFile(psiFile: PsiFile): Option[VirtualFile] =
    Option(psiFile.getVirtualFile)
      .orElse(Option(psiFile.getViewProvider.getVirtualFile))
      .flatMap {
        case light: LightVirtualFile => Option(light.getOriginalFile)
        case _ => None
      }

  def getScalaVersion(file: PsiFile): Option[String] =
    findVirtualFile(file)
      .flatMap(getModuleForFile(_, file.getProject))
      .flatMap(_.scalaSdk)
      .flatMap(_.compilerVersion)

  def getModuleForFile(virtualFile: VirtualFile, project: Project): Option[Module] =
    Option(ProjectRootManager.getInstance(project).getFileIndex.getModuleForFile(virtualFile))

  def getModuleForFile(file: PsiFile): Option[Module] =
    Option(file.getVirtualFile)
      .flatMap(getModuleForFile(_, file.getProject))
      .orElse(file.getProject.modulesWithScala.headOption)
}