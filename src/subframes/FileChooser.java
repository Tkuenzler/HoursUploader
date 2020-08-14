package subframes;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class FileChooser {
	
	public static File OpenXlsFile(String title) {
		JFileChooser fc = new JFileChooser(".");
		fc.setDialogTitle(title);
		fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
	        public boolean accept(File f) {
		          return f.getName().toLowerCase().endsWith(".xls") || f.isDirectory();
		        }
		        public String getDescription() {
		          return "Excel Files";
		        }
		    });
		int r = fc.showOpenDialog(new JFrame());
		if (r == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		}
		else
			return null;
	}
}
