// This file is part of the "IBController".
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2011 Richard L King (rlking@aultan.com)
// For conditions of distribution and use, see copyright notice in COPYING.txt

// IBController is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// IBController is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with IBController.  If not, see <http://www.gnu.org/licenses/>.

package ibcontroller;

import java.awt.Component;
import java.awt.Container;
import javax.swing.*;

class ConfigureTwsApiPortTask implements Runnable{
    
    final int mPortNumber;
    
    ConfigureTwsApiPortTask(int portNumber) {
        mPortNumber = portNumber;
    }

    @Override
    public void run() {
        try {
            final JDialog configDialog = TwsListener.getConfigDialog();    // blocks the thread until the config dialog is available
            
            GuiExecutor.instance().execute(new Runnable(){
                @Override
                public void run() {configure(configDialog, mPortNumber);}
            });

        } catch (Exception e){
            Utils.logError("IBController: " + e.getMessage());
        }
    }

    private void configure(final JDialog configDialog, final int portNumber) {
        try {
            Utils.logToConsole("Performing port configuration");
            
            if (!TwsListener.selectConfigSection(configDialog, new String[] {"API","Settings"}))
                // older versions of TWS don't have the Settings node below the API node
                TwsListener.selectConfigSection(configDialog, new String[] {"API"});

            Component comp = Utils.findComponent(configDialog, "Socket port");
            if (comp == null) throw new IBControllerException("could not find socket port component");

            JTextField tf = Utils.findTextField((Container)comp, 0);
            if (tf == null) throw new IBControllerException("could not find socket port field");
            
            int currentPort = Integer.parseInt(tf.getText());
            if (currentPort == portNumber) {
                Utils.logToConsole("TWS API socket port is already set to " + tf.getText());
            } else {
                if (!IBController.isGateway()) {
                    JCheckBox cb = Utils.findCheckBox(configDialog, "Enable ActiveX and Socket Clients");
                    if (cb == null) throw new IBControllerException("could not find Enable ActiveX checkbox");
                    if (cb.isSelected()) TwsListener.setApiConfigChangeConfirmationExpected(true);
                }
                Utils.logToConsole("TWS API socket port was set to " + tf.getText());
                tf.setText(new Integer(portNumber).toString());
                Utils.logToConsole("TWS API socket port now set to " + tf.getText());
            }

            Utils.clickButton(configDialog, "OK");

            configDialog.setVisible(false);
        } catch (IBControllerException e) {
            Utils.logError("IBController: " + e.getMessage());
        }
    }
}
