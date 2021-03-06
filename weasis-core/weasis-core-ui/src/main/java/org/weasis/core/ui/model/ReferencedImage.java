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
package org.weasis.core.ui.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;

import org.weasis.core.ui.model.utils.imp.DefaultUUID;

public class ReferencedImage extends DefaultUUID {
    private static final long serialVersionUID = 634321872759432378L;

    private List<Integer> frames;

    public ReferencedImage() {
        this(null, null);
    }

    public ReferencedImage(String uuid) {
        this(uuid, null);
    }

    public ReferencedImage(String uuid, List<Integer> frames) {
        super(uuid);
        setFrames(frames);
    }

    @XmlList
    @XmlAttribute(name = "frames", required = false)
    public List<Integer> getFrames() {
        return frames;
    }

    /**
     * Set a list of frames. Each value matches to the position of a MediaElement (media.getKey()).
     * 
     * Note: to match with DICOM Instance Frame the value must be increment of one (DicomFrame = frame + 1).
     * 
     * @param frames
     */
    public void setFrames(List<Integer> frames) {
        this.frames = Optional.ofNullable(frames).orElseGet(ArrayList::new);
    }

}
