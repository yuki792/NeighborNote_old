/*
 * This file is part of NixNote 
 * Copyright 2011 Randy Baumgarte
 * 
 * This file may be licensed under the terms of of the
 * GNU General Public License Version 2 (the ``GPL'').
 *
 * Software distributed under the License is distributed
 * on an ``AS IS'' basis, WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the GPL for the specific language
 * governing rights and limitations.
 *
 * You should have received a copy of the GPL along with this
 * program. If not, go to http://www.gnu.org/licenses/gpl.html
 * or write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
*/

package cx.fbn.nevernote.gui;

import com.trolltech.qt.gui.QTreeWidgetItem;

public class NTreeWidgetItem extends QTreeWidgetItem {
	@Override
	public boolean operator_less(QTreeWidgetItem other) {
		if (text(0).toLowerCase().compareTo(other.text(0).toLowerCase()) < 0)
			return true;
		else
			return false;
	}
}
