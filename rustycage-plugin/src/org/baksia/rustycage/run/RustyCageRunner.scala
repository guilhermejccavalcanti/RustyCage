package org.baksia.rustycage.run

import org.eclipse.ui.progress.IProgressService
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IContainer

object RustyCageRunner {

  import scala.sys.process._
  def run(iFile: IFile, progressService: IProgressService) {
    val crate = findCrate(iFile.getParent)
    val rawPath = crate.getRawLocationURI.getRawPath
    val file: String = rawPath.substring(0, rawPath.lastIndexOf(".")).replaceFirst("/src", "/bin").replaceFirst("/test", "/bin")

    val messageConsole = new MessageConsoleScala(iFile, "Run : ")
    messageConsole.message("Running: " + file)
    val logger = ProcessLogger(
      (o: String) => messageConsole.message(o),
      (e: String) => messageConsole.errorMessage(e))

    file ! logger
    messageConsole.close()

  }

  //Move this function out a trait CrateFinder or in RustPlugin ??
  def findCrate(dir: IContainer): IResource = {
    val crates = dir.members().filter(r => r.getFileExtension == "rc")
    if(crates.length > 1)
      throw new RuntimeException
    else
      crates(0)
  }
}
