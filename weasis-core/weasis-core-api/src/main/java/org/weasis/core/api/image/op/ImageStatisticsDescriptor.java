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
package org.weasis.core.api.image.op;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RenderedRegistryMode;

public class ImageStatisticsDescriptor extends OperationDescriptorImpl implements RenderedImageFactory {

    /**
     * The resource strings that provide the general documentation and specify the parameter list for this operation.
     */
    private static final String[][] resources = { { "GlobalName", "ImageStatistics" }, //$NON-NLS-1$ //$NON-NLS-2$

        { "LocalName", "ImageStatistics" }, //$NON-NLS-1$ //$NON-NLS-2$

        { "Vendor", "" }, //$NON-NLS-1$ //$NON-NLS-2$

        { "Description", //$NON-NLS-1$
            "Finds the min, max and mean of pixel values in each band of an image with the option to exclude a range of values." }, //$NON-NLS-1$

        { "DocURL", "" }, //$NON-NLS-1$ //$NON-NLS-2$
        { "Version", "1.0" }, //$NON-NLS-1$ //$NON-NLS-2$

        { "arg0Desc", "The region of the image to scan" }, //$NON-NLS-1$ //$NON-NLS-2$

        { "arg1Desc", "The horizontal sampling rate, may not be less than 1." }, //$NON-NLS-1$ //$NON-NLS-2$

        { "arg2Desc", "The vertical sampling rate, may not be less than 1." }, //$NON-NLS-1$ //$NON-NLS-2$

        { "arg3Desc", "The lowest value to exclude" }, //$NON-NLS-1$ //$NON-NLS-2$

        { "arg4Desc", "The highest value to exclude" } }; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * The modes that this operator supports. maybe one or more of "rendered", "renderable", "collection", and
     * "renderableCollection".
     */
    private static final String[] supportedModes = { "rendered" }; //$NON-NLS-1$

    /** The parameter name list for this operation. */
    private static final String[] paramNames = { "roi", "xPeriod", "yPeriod", "excludedMin", "excludedMax" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    /** The parameter class list for this operation. */
    private static final Class[] paramClasses = { javax.media.jai.ROI.class, java.lang.Integer.class,
        java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class };

    /** The parameter default value list for this operation. */
    private static final Object[] paramDefaults = { null, 1, 1, null, null };

    /** Constructor. */
    public ImageStatisticsDescriptor() {
        super(resources, supportedModes, 1, paramNames, paramClasses, paramDefaults, null);
    }

    @Override
    public RenderedImage create(ParameterBlock paramBlock, RenderingHints renderHints) {
        if (!validateSources(paramBlock)) {
            return null;
        }
        if (!validateParameters(paramBlock)) {
            return null;
        }

        RenderedImage renderedimage = paramBlock.getRenderedSource(0);
        int i = renderedimage.getMinX();
        int j = renderedimage.getMinY();

        return new ImageStatisticsOpImage(renderedimage, (ROI) paramBlock.getObjectParameter(0), i, j,
            (Integer) paramBlock.getObjectParameter(1), (Integer) paramBlock.getObjectParameter(2),
            (Double) paramBlock.getObjectParameter(3), (Double) paramBlock.getObjectParameter(4));
    }

    public boolean validateSources(ParameterBlock parameterblock) {
        return parameterblock.getRenderedSource(0) != null;
    }

    public boolean validateParameters(ParameterBlock paramBlock) {
        Object arg = paramBlock.getObjectParameter(0);
        if (!(arg instanceof ROI) && arg != null) {
            return false;
        }
        return true;
    }

    public static RenderedOp create(RenderedImage source0, ROI roi, Integer xPeriod, Integer yPeriod,
        Double excludedMin, Double excludedMax, RenderingHints hints) {
        ParameterBlockJAI pb = new ParameterBlockJAI("ImageStatistics", RenderedRegistryMode.MODE_NAME); //$NON-NLS-1$
        pb.setSource("source0", source0); //$NON-NLS-1$
        pb.setParameter("roi", roi); //$NON-NLS-1$
        pb.setParameter("xPeriod", xPeriod); //$NON-NLS-1$
        pb.setParameter("yPeriod", yPeriod); //$NON-NLS-1$
        pb.setParameter("excludedMin", excludedMin); //$NON-NLS-1$
        pb.setParameter("excludedMax", excludedMax); //$NON-NLS-1$
        return JAI.create("ImageStatistics", pb, hints); //$NON-NLS-1$
    }
}
