/*******************************************************************************
 * Copyright (c) 2015 Weasis Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.openjpeg.internal;

import javax.imageio.spi.ImageReaderSpi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.weasis.openjpeg.NativeJ2kImageReaderSpi;

import com.sun.media.imageioimpl.common.ImageioUtil;

public class Activator implements BundleActivator {

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        // Should give the priority to other j2k codecs
        ImageioUtil.registerServiceProviderPriority(NativeJ2kImageReaderSpi.class, ImageReaderSpi.class,
            NativeJ2kImageReaderSpi.NAMES[0]);
        // ImageioUtil.registerServiceProvider(NativeJ2kImageWriterSpi.class);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        ImageioUtil.unRegisterServiceProvider(NativeJ2kImageReaderSpi.class);
        // ImageioUtil.unRegisterServiceProvider(NativeJ2kImageWriterSpi.class);
    }

}
