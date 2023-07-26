package cz.wa.smoothmetalmap.gui.texturecanvas

import cz.wa.smoothmetalmap.SmoothMetalMapMain
import cz.wa.smoothmetalmap.gui.ContentHolder
import cz.wa.smoothmetalmap.gui.MainFrame
import cz.wa.smoothmetalmap.gui.utils.GuiUtils
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

/**
 * Allows drag and drop images from OS
 */
class DropTextureViewer(contentHolder: ContentHolder) : TextureViewer(contentHolder) {
    private var dropTarget: DropTarget? = null
    private var dropTargetHandler: DropTargetHandler? = null
    private val listeners = HashSet<(File, BufferedImage) -> Unit>()

    fun addListener(l: (File, BufferedImage) -> Unit) {
        listeners.add(l)
    }

    fun removeListener(l: (File, BufferedImage) -> Unit) {
        listeners.remove(l)
    }

    private fun importFiles(files: List<File>) {
        for (file in files) {
            if (SmoothMetalMapMain.IMAGE_OPEN_EXTS.contains(file.extension.lowercase())) {
                GuiUtils.runCatch(MainFrame.instance!!) {
                    customImage = ImageIO.read(file)
                    refresh()
                    for (l in listeners) {
                        l.invoke(file, customImage!!)
                    }
                }
            }
        }
    }

    protected fun getMyDropTarget(): DropTarget? {
        if (dropTarget == null) {
            dropTarget = DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, null)
        }
        return dropTarget
    }

    protected fun getDropTargetHandler(): DropTargetHandler? {
        if (dropTargetHandler == null) {
            dropTargetHandler = DropTargetHandler(this)
        }
        return dropTargetHandler
    }

    override fun addNotify() {
        super.addNotify()
        try {
            getMyDropTarget()?.addDropTargetListener(getDropTargetHandler())
        } catch (ex: TooManyListenersException) {
            ex.printStackTrace()
        }
    }

    override fun removeNotify() {
        super.removeNotify()
        getMyDropTarget()?.removeDropTargetListener(getDropTargetHandler())
    }

    protected class DropTargetHandler(private val viewer: DropTextureViewer) : DropTargetListener {
        protected fun processDrag(dtde: DropTargetDragEvent) {
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY)
            } else {
                dtde.rejectDrag()
            }
        }

        override fun dragEnter(dtde: DropTargetDragEvent) {
            processDrag(dtde)
            //SwingUtilities.invokeLater(DragUpdate(true, dtde.location))
            viewer.refresh()
        }

        override fun dragOver(dtde: DropTargetDragEvent) {
            processDrag(dtde)
            //SwingUtilities.invokeLater(DragUpdate(true, dtde.location))
            viewer.refresh()
        }

        override fun dropActionChanged(dtde: DropTargetDragEvent) {}

        override fun dragExit(dte: DropTargetEvent) {
            //SwingUtilities.invokeLater(DragUpdate(false, null))
            viewer.refresh()
        }

        override fun drop(dtde: DropTargetDropEvent) {
            //SwingUtilities.invokeLater(DragUpdate(false, null))
            val transferable = dtde.transferable
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrop(dtde.dropAction)
                try {
                    val transferData = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                    if (transferData != null && transferData.size > 0) {
                        viewer.importFiles(transferData as List<File>)
                        dtde.dropComplete(true)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            } else {
                dtde.rejectDrop()
            }
        }
    }
}