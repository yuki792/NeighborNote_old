// ICHANGED
package cx.fbn.nevernote.dialog;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QFormLayout;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QSlider;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.Global;

public class ConfigRensoListPage extends QWidget {
	QSlider browseSlider;
	QSlider copyPasteSlider;
	QSpinBox browseSpinner;
	QSpinBox copyPasteSpinner;
	
	public ConfigRensoListPage(QWidget parent) {
		copyPasteSlider = new QSlider();
		
		browseSlider = new QSlider();
		browseSlider.setOrientation(Qt.Orientation.Horizontal);
		browseSlider.setRange(0, 10);
		browseSlider.setSingleStep(1);
		browseSlider.setTickPosition(QSlider.TickPosition.TicksAbove);
		browseSlider.setTickInterval(1);
		browseSlider.setFocusPolicy(Qt.FocusPolicy.StrongFocus);

		browseSpinner = new QSpinBox();
		browseSpinner.setRange(0,10);
		browseSpinner.setSingleStep(1);
		
		browseSlider.valueChanged.connect(browseSpinner, "setValue(int)");
		browseSpinner.valueChanged.connect(browseSlider, "setValue(int)");
		browseSlider.setValue(Global.getBrowseWeight());
		
		QHBoxLayout browseLayout = new QHBoxLayout();
		browseLayout.addWidget(browseSlider);
		browseLayout.addWidget(browseSpinner);
		
		
		copyPasteSlider = new QSlider();
		copyPasteSlider.setOrientation(Qt.Orientation.Horizontal);
		copyPasteSlider.setRange(0, 10);
		copyPasteSlider.setSingleStep(1);
		copyPasteSlider.setTickPosition(QSlider.TickPosition.TicksAbove);
		copyPasteSlider.setTickInterval(1);
		copyPasteSlider.setFocusPolicy(Qt.FocusPolicy.StrongFocus);
		
		copyPasteSpinner = new QSpinBox();
		copyPasteSpinner.setRange(0,10);
		copyPasteSpinner.setSingleStep(1);
		
		copyPasteSlider.valueChanged.connect(copyPasteSpinner, "setValue(int)");
		copyPasteSpinner.valueChanged.connect(copyPasteSlider, "setValue(int)");
		copyPasteSlider.setValue(Global.getCopyPasteWeight());

		
		QHBoxLayout copyPasteLayout = new QHBoxLayout();
		copyPasteLayout.addWidget(copyPasteSlider);
		copyPasteLayout.addWidget(copyPasteSpinner);
		
		QFormLayout styleLayout = new QFormLayout();
		styleLayout.setHorizontalSpacing(10);
		styleLayout.setVerticalSpacing(30);
		styleLayout.addRow(tr("Browse Weight"), browseLayout);
		styleLayout.addRow(tr("Copy&Paste Weight"), copyPasteLayout);

		QGroupBox weightingGroup = new QGroupBox(tr("Weighting"));
		weightingGroup.setLayout(styleLayout);
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addWidget(weightingGroup);
		mainLayout.addStretch(1);
		setLayout(mainLayout);
		
	}
	
	//*****************************************
	//* Browse Weight 
	//*****************************************
	public void setBrowseWeight(int weight) {
		browseSpinner.setValue(weight);
	}
	public int getBrowseWeight() {
		return browseSpinner.value();
	}
	
	//*****************************************
	//* Copy & Paste Weight 
	//*****************************************
	public void setcopyPasteWeight(int weight) {
		copyPasteSpinner.setValue(weight);
	}
	public int getcopyPasteWeight() {
		return copyPasteSpinner.value();
	}
}
