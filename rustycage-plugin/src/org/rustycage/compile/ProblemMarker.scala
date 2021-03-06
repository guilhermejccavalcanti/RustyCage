package org.rustycage.compile

import org.eclipse.core.resources.{IFile, IMarker, IResource}
import org.eclipse.core.runtime.{CoreException, IStatus}
import org.rustycage.RustPlugin
import org.rustycage.run.MessageConsoleScala

trait ProblemMarker extends MessageConsoleScala {
  val MARKER_TYPE = "org.eclipse.core.resources.problemmarker"

  if (file.getFileExtension == "rc") clearAllMarkers(file) else clearMarkers(file)

  override def errorMessage(message: String) {
    super.errorMessage(message)
    addProblemMarkers(message)
  }

  override def message(message: String) {
    super.message(message)
  }

  private[compile] def addProblemMarkers(message: String) {
    parseProblemFirstLine(message, file)
  }

  private[compile] def parseProblemSecondLine(errorString: String, file: IFile) {
    val theFile = findCorrectFile(errorString, file)
    if (errorString.contains(".rs")) {
      val tokens: Array[String] = errorString.split(":")
      val markerString: String = tokens(tokens.length - 1) + ": " + tokens(tokens.length - 2)
      var lineNumber: Int = 0
      try {
        lineNumber = Integer.parseInt(tokens(1).replaceAll("[^\\d]", ""))
      } catch {
        case exception: NumberFormatException =>
          lineNumber = 0
      }
      addMarker(theFile, markerString, lineNumber, IMarker.SEVERITY_ERROR)
    }
  }

  private[compile] def parseProblemFirstLine(errorString: String, file: IFile) {
    val theFile = findCorrectFile(errorString, file)
    if (errorString.contains(".rs") && errorString.contains("error")) {
      val tokens: Array[String] = errorString.split(":")
      val markerString: String = tokens(tokens.length - 1) + ": " + tokens(tokens.length - 2)
      var lineNumber: Int = 0
      try {
        lineNumber = Integer.parseInt(tokens(1))
      } catch {
        case exception: NumberFormatException =>
          lineNumber = 0
      }
      addMarker(theFile, markerString, lineNumber, IMarker.SEVERITY_ERROR)
    }
  }

  private[compile] def addMarker(file: IFile, message: String, lineNumberInn: Int, severity: Int) {
    try {
      var lineNumber = lineNumberInn
      val marker = file.createMarker(MARKER_TYPE)
      marker.setAttribute(IMarker.MESSAGE, message)
      marker.setAttribute(IMarker.SEVERITY, severity)

      if (lineNumber == -1) {
        lineNumber = 1
      }
      marker.setAttribute(IMarker.LINE_NUMBER, lineNumber)

    } catch {
      case e: CoreException =>
        RustPlugin.log(IStatus.ERROR, RustPlugin.PluginId, e)
    }
  }

  private[compile] def clearMarkers(file: IFile) {
    try {
      file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO)
    } catch {
      case e: CoreException =>
        RustPlugin.log(IStatus.ERROR, RustPlugin.PluginId, e)
    }
  }

  def clearAllMarkers(file: IFile) {
    file.getParent.members.foreach(file => file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO))
  }

  private def findCorrectFile(firstLine: String, file: IFile): IFile = {
    if (file.getFileExtension == "rc") {
      val resources: Array[IResource] = file.getParent.members
      for (resource <- resources) {
        if (firstLine.contains(resource.getName)) {
          return resource.asInstanceOf[IFile]
        }
      }
    }
    file
  }
  /*private[compile] def findCorrectFile(firstLine: String, file: IFile): IFile = {
    if (file.getFileExtension == "rc") {
      for (resource <- file.getParent.members; if firstLine.contains(resource.getName)) yield resource.asInstanceOf[IFile]
    }
    file
  }*/
}
