/*******************************************************************************
 * Copyright (c) 2016 Weasis Team and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 *******************************************************************************/
package org.weasis.core.api.gui.task;

import java.awt.Component;

import javax.swing.ProgressMonitor;

public class TaskMonitor extends ProgressMonitor {

    private boolean showProgression;

    public TaskMonitor(Component parentComponent, Object message, String note, int min, int max) {
        super(parentComponent, message, note, min, max);
        this.showProgression = true;
    }

    public boolean isShowProgression() {
        return showProgression;
    }

    public void setShowProgression(boolean showProgression) {
        this.showProgression = showProgression;
    }

}
